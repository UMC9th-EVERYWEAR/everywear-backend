package com.umc.EveryWear.domain.fitting.service.command.job;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@RequiredArgsConstructor
public class FittingJobPublisher {

    private final SqsAsyncClient sqsAsyncClient;

    @Value("${SQS_FITTING_QUEUE_URL}")
    private String queueUrl;

    public void publish(Long fittingHistoryId) {

        sqsAsyncClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(String.valueOf(fittingHistoryId))
                        .build()
        );
    }
}
