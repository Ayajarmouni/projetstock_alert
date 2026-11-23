
package com.stockalert.approvisionnement.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class FeignConfiguration {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErreurDecoder();
    }

    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }
}
