package com.sho1kat.mapper;

import com.sho1kat.entity.FareRule;
import com.sho1kat.payload.pricingServiceDto.request.FareRuleRequest;
import com.sho1kat.payload.pricingServiceDto.response.FareRuleResponse;
import org.springframework.stereotype.Component;

@Component
public class FareRuleMapper {

    public FareRule toEntity(FareRuleRequest request) {

        FareRule rule = new FareRule();

        rule.setName(request.getName().trim());
        rule.setRefundable(Boolean.TRUE.equals(request.getRefundable()));
        rule.setRefundPercent(request.getRefundPercent());
        rule.setChangeFee(request.getChangeFee());
        rule.setMinAdvanceDays(request.getMinAdvanceDays());
        rule.setMaxStayDays(request.getMaxStayDays());
        rule.setCancelCutoffHours(request.getCancelCutoffHours());
        rule.setNoShowPenalty(request.getNoShowPenalty());
        rule.setActive(request.getIsActive() == null || request.getIsActive());

        return rule;
    }

    public void updateEntity(FareRuleRequest request, FareRule existing) {

        if (request == null || existing == null) {
            return;
        }

        existing.setName(request.getName().trim());
        existing.setRefundable(Boolean.TRUE.equals(request.getRefundable()));
        existing.setRefundPercent(request.getRefundPercent());
        existing.setChangeFee(request.getChangeFee());
        existing.setMinAdvanceDays(request.getMinAdvanceDays());
        existing.setMaxStayDays(request.getMaxStayDays());
        existing.setCancelCutoffHours(request.getCancelCutoffHours());
        existing.setNoShowPenalty(request.getNoShowPenalty());

        if (request.getIsActive() != null) {
            existing.setActive(request.getIsActive());
        }
    }

    public FareRuleResponse toResponse(FareRule rule) {

        if (rule == null) {
            return null;
        }

        return FareRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .refundable(rule.isRefundable())
                .refundPercent(rule.getRefundPercent())
                .changeFee(rule.getChangeFee())
                .minAdvanceDays(rule.getMinAdvanceDays())
                .maxStayDays(rule.getMaxStayDays())
                .cancelCutoffHours(rule.getCancelCutoffHours())
                .noShowPenalty(rule.getNoShowPenalty())
                .isActive(rule.isActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
