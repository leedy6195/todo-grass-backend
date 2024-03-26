package com.oxingaxin.todograss.common.aws

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AwsConfiguration {

    @Value("\${aws.access-key}")
    private lateinit var accessKey: String

    @Value("\${aws.secret-key}")
    private lateinit var secretKey: String

    @Value("\${aws.s3.region}")
    private lateinit var region: String

    @Bean
    fun amazonS3Client(): AmazonS3 {
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        return AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.fromName(region))
            .build()
    }
}