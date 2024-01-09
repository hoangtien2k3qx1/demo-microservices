package com.hoangtien2k3.notificationservice.event;

import com.google.gson.Gson;
import com.hoangtien2k3.notificationservice.constant.KafkaConstant;
import com.hoangtien2k3.notificationservice.dto.EmailDetails;
import com.hoangtien2k3.notificationservice.dto.PaymentDto;
import com.hoangtien2k3.notificationservice.service.EmailService;
import com.hoangtien2k3.notificationservice.service.NotificationService;
import com.hoangtien2k3.notificationservice.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;
import java.util.function.Consumer;

@Service
@Slf4j
public class EventConsumer {

    Gson gson = new Gson(); // convert Json -> DTO

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EventProducer eventProducer;

//    public EventConsumer(ReceiverOptions<String, String> receiverOptions) {
//        KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(KafkaConstant.PROFILE_ONBOARDING_TOPIC)))
//                .receive()
//                .subscribe(this::sendEmailKafkaOnboarding);
//    }


    public EventConsumer(ReceiverOptions<String, String> receiverOptions) {
        subscribeToTopic(receiverOptions, KafkaConstant.PROFILE_ONBOARDING_TOPIC, this::sendEmailKafkaOnboarding);
        subscribeToTopic(receiverOptions, KafkaConstant.STATUS_PAYMENT_SUCCESSFUL, this::paymentOrderKafkaOnboarding);

    }

    private void subscribeToTopic(ReceiverOptions<String, String> receiverOptions, String topic, Consumer<ReceiverRecord<String, String>> handler) {
        KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(topic)))
                .receive()
                .subscribe(handler);
        log.info("Subscribed to Kafka topic: {}", topic);
    }


    public void sendEmailKafkaOnboarding(ReceiverRecord<String, String> receiverRecord) {
        log.info("USER-SERVICE Onboarding event send email on notification service.");
        EmailDetails emailDetails = gson.fromJson(receiverRecord.value(), EmailDetails.class);

        emailService.sendSimpleMail(emailDetails).subscribe(email -> {
            log.info("send email successfully -> user-service change password.");
            eventProducer.send(KafkaConstant.PROFILE_ONBOARDED_TOPIC, gson.toJson(email)).subscribe();
        });
    }


    public void paymentOrderKafkaOnboarding(ReceiverRecord<String, String> receiverRecord) {
        log.info("Profile Onboarding event");

        PaymentDto paymentDto = gson.fromJson(receiverRecord.value(), PaymentDto.class);
        paymentService.savePayment(paymentDto).subscribe(res -> {

            eventProducer.send(KafkaConstant.PROFILE_ONBOARDED_TOPIC, gson.toJson(paymentDto)).subscribe();
        });

    }


}