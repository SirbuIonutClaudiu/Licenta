package com.bezkoder.spring.security.postgresql.awsecrets;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    private final Gson gson = new Gson();

    @Bean
    public DataSource dataSource() {
        AwsSecrets secrets = getSecret();
        assert secrets != null;
        return DataSourceBuilder
                .create()
                .url("jdbc:" + secrets.getEngine() + "ql://" + secrets.getHost() + ":" + secrets.getPort() + "/UnitbVotingDB")
                .username(secrets.getUsername())
                .password(secrets.getPassword())
                .build();
    }

    private AwsSecrets getSecret() {
        String secretName = "UnitbVotindAppDBcredentials";
        String region = "us-east-2";
        String accessKey = "AKIAYYPCZEEQOCTVG5XK";
        String secretkey = "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";

        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretkey)))
                .build();

        String secret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult;

        getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
            return gson.fromJson(secret, AwsSecrets.class);
        }
        return null;
    }
}
