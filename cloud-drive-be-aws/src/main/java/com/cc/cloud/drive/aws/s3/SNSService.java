package com.cc.cloud.drive.aws.s3;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SNSService {
    private static final String SNS_TOPIC_PREFIX = "cloud-drive-sns-topic-";
    @Autowired
    private AmazonSNS snsClient;

    public String createTopicForUser(String username, String email) {
        CreateTopicRequest createTopicRequest = new CreateTopicRequest(SNS_TOPIC_PREFIX + username);
        CreateTopicResult createTopicResponse = snsClient.createTopic(createTopicRequest);
        SubscribeRequest subscribeRequest = new SubscribeRequest(createTopicResponse.getTopicArn(), "email", email);

        snsClient.subscribe(subscribeRequest);
        return createTopicResponse.getTopicArn();
    }
}
