package com.welabs.monitoring.service;

import com.welabs.monitoring.entity.StudentMaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TeamsNotifier {

    private final RestClient restClient = RestClient.create();

    @Value("${notification.teams.webhook-url:}")
    private String webhookUrl;

    public boolean notifyAbsenceLong(StudentMaster student, long elapsedSec) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("Teams webhook URL 미설정, 발송 스킵 (DB 마킹은 진행)");
            return true;
        }

        long min = elapsedSec / 60;
        long sec = elapsedSec % 60;

        Map<String, Object> payload = Map.of(
            "@type", "MessageCard",
            "@context", "https://schema.org/extensions",
            "themeColor", "F44336",
            "summary", "자리비움 알림",
            "title", "🔴 " + student.getName() + " 자리비움 " + min + "분 " + sec + "초",
            "text", String.format(
                "**기수**: %s  \n**학생ID**: %s  \n**자산**: %s",
                student.getCohort(),
                student.getStudentId(),
                student.getAssetCode() != null ? student.getAssetCode() : "-")
        );

        try {
            restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("Teams notify 실패: {}", e.getMessage());
            return false;
        }
    }

    public boolean notifySpoofSuspected(StudentMaster student, int score, List<String> reasons) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("Teams webhook URL 미설정, 위장 알림 스킵 (DB 저장은 진행)");
            return true;
        }

        String reasonText = (reasons != null && !reasons.isEmpty())
                ? String.join(", ", reasons)
                : "정보 없음";

        Map<String, Object> payload = Map.of(
            "@type", "MessageCard",
            "@context", "https://schema.org/extensions",
            "themeColor", "9C27B0",
            "summary", "위장 의심 감지",
            "title", "🎭 " + student.getName() + " 위장 의심 (점수 " + score + ")",
            "text", String.format(
                "**기수**: %s  \n**학생ID**: %s  \n**자산**: %s  \n**근거**: %s",
                student.getCohort(),
                student.getStudentId(),
                student.getAssetCode() != null ? student.getAssetCode() : "-",
                reasonText)
        );

        try {
            restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("Spoof Teams notify 실패: {}", e.getMessage());
            return false;
        }
    }
}
