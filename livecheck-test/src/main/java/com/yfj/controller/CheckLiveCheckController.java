package com.yfj.controller;

import com.yfj.module.Availability;
import com.yfj.module.BaseResponse;
import com.yfj.servcie.CheckLiveCheckService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping(value = "/check/liveCheck")
public class CheckLiveCheckController {
    @Autowired
    CheckLiveCheckService checkLiveCheckService;

    @PostMapping(value = "/migrate/validate")
    public BaseResponse obtainProductInfo(@RequestBody Availability availability) {
        try {
           boolean validateFlag= checkLiveCheckService.traverseHotel(availability.getSupplierId(), availability.getDistributorId());
            return BaseResponse.success(validateFlag);
        } catch (Throwable ex) {
            log.error("Exception when validating supplier liveCheck data", ex);
            return BaseResponse.fail(ex.getMessage());
        }
    }

}
