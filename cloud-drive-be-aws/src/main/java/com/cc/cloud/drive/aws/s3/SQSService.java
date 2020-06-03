package com.cc.cloud.drive.aws.s3;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SQSService {
    private static final String SQS_TOPIC = "cloud-drive-sqs.fifo";
    private static final String SQS_URL = "https://sqs.us-east-1.amazonaws.com/366180546911/cloud-drive-sqs.fifo";
    private static final String SQS_MESSAGE_GROUP = "cloud-drive";

    @Autowired
    private AmazonSQS sqsClient;

    public void send(String message) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest(SQS_URL, message);

        sendMessageRequest.setMessageGroupId(SQS_MESSAGE_GROUP);
        sqsClient.sendMessage(sendMessageRequest);
    }
}
