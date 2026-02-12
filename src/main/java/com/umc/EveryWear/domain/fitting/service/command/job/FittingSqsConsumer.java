package com.umc.EveryWear.domain.fitting.service.command.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class FittingSqsConsumer {

    private final SqsAsyncClient sqsAsyncClient;
    private final FittingProcessor fittingProcessor;

    @Value("${SQS_FITTING_QUEUE_URL}")
    private String queueUrl;

    @Scheduled(fixedDelay = 3000)
    public void pollQueue() {

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(5)
                .waitTimeSeconds(10)
                .build();

        sqsAsyncClient.receiveMessage(request)
                .thenAccept(response -> {
                    response.messages().forEach(message -> {
                        try {
                            Long id = Long.parseLong(message.body());
                            fittingProcessor.process(id);

                            deleteMessage(message.receiptHandle());
                        } catch (Exception e) {
                            log.error("SQS 처리 실패", e);
                        }
                    });
                });
    }

    private void deleteMessage(String receiptHandle) {
        sqsAsyncClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build());
    }
}