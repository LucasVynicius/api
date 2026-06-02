package com.eventostec.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final AmazonS3 s3Client;
    private final EventRepository eventRepository;
    private final AddressService addressService;
    private final CouponService couponService;

    public Event createEvent(EventRequestDTO data){
        String imgUrl = null;

        if (data.imgUrl() != null){
            imgUrl = this.uploadImg(data.imgUrl());
        }

        Event newEvent = new Event();
        newEvent.setTitle(data.title());
        newEvent.setDescription(data.description());
        newEvent.setEventUrl(data.eventUrl());
        newEvent.setRemote(data.remote());
        newEvent.setDate(new Date(data.date()));
        newEvent.setImgUrl(imgUrl);

        eventRepository.save(newEvent);

        if (Boolean.FALSE.equals(data.remote())){
            this.addressService.createAddress(data, newEvent);
        }

        return newEvent;
    }

    public List<EventResponseDTO> getUpcomingEvents(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = this.eventRepository.findUpcomingEvent(new Date(),pageable);
        return eventsPage.map(event -> new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress() != null ? event.getAddress().getCity() : "",
                event.getAddress() != null ? event.getAddress().getUf() :
                        "",
                event.getRemote(),
                event.getEventUrl(),
                event.getImgUrl()
        )).stream().toList();
    }

    public List<EventResponseDTO> getFilteredEvents(int page, int size, String title, String city, String uf, Date startDate, Date endDate){
        startDate = (startDate != null) ? startDate : new Date(0);
        endDate =  (endDate != null) ? endDate : new Date(253402300799000L);

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = this.eventRepository.findFilteredEvents(title, city, uf, startDate, endDate, pageable);
        return eventsPage.map(event -> new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress() != null ? event.getAddress().getCity() : "",
                event.getAddress() != null ? event.getAddress().getUf() :
                "",
                event.getRemote(),
                event.getEventUrl(),
                event.getImgUrl()
        )).stream().toList();
    }

    public EventDetailsDTO getEventDetails(UUID eventId){
        Event event = this.eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));

        List<Coupon> coupons = couponService.consultCoupons(eventId
        , new Date());

        List<EventDetailsDTO.CouponDTO> couponDTOs = coupons.stream().map(coupon -> new EventDetailsDTO.CouponDTO(
                coupon.getCode(),
                coupon.getDiscount(),
                coupon.getValid()
        )).collect(Collectors.toList());

        return new EventDetailsDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress() != null ? event.getAddress().getCity() : "",
                event.getAddress() != null ? event.getAddress().getUf() :
                        "",
                event.getImgUrl(),
                event.getEventUrl(),
                couponDTOs

        );
    }

    private String uploadImg(MultipartFile multipartFile){
        String originalFilename = multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename() : "event-image";
        String fileName = UUID.randomUUID() + "-" + originalFilename;
        File file = null;
        try{
            file = this.convertMultipartToFile(multipartFile);
            s3Client.putObject(bucketName, fileName, file);
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch(Exception e){
            System.out.println("Error uploading file: " + e.getMessage());
            return null;
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }

    }

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename() : "event-image";
        Path tempFile = Files.createTempFile("event-upload-", "-" + originalFilename);
        multipartFile.transferTo(tempFile);
        return tempFile.toFile();
    }
}
