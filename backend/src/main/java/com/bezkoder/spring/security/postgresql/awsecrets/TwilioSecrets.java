package com.bezkoder.spring.security.postgresql.awsecrets;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import lombok.Data;

@Data
public class TwilioSecrets {
    private String secretName;
    private String region;

    public TwilioSecrets(String secretName) {
        this.secretName = secretName;
        this.region = "us-east-2";
    }

    private String JsonToString(String JsonString) {
        return JsonString.substring(JsonString.indexOf(':')+2, JsonString.length()-2);
    }

    public String getSecret() {
        String accessKey = "-";
        String secretkey = "-";

        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(this.region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretkey)))
                .build();

        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(this.secretName);
        GetSecretValueResult getSecretValueResult;

        getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        if (getSecretValueResult.getSecretString() != null) {
            return JsonToString(getSecretValueResult.getSecretString());
        }
        return null;
    }
}
