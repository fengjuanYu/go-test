package com.yfj.servcie;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.derbysoft.dswitch.dto.hotel.avail.AvailRoomStayDTO;
import com.derbysoft.dswitch.dto.hotel.avail.HotelAvailCriteriaDTO;
import com.derbysoft.dswitch.dto.hotel.avail.HotelAvailRQ;
import com.derbysoft.dswitch.dto.hotel.avail.RoomStayCandidateDTO;
import com.derbysoft.dswitch.dto.hotel.common.RateDTO;
import com.derbysoft.dswitch.dto.hotel.common.StayDateRangeDTO;
import com.derbysoft.dswitch.remote.hotel.buyer.DefaultHotelBuyerRemoteService;
import com.derbysoft.dswitch.remote.hotel.dto.HotelAvailRequest;
import com.derbysoft.dswitch.remote.hotel.dto.HotelAvailResponse;
import com.derbysoft.dswitch.remote.hotel.dto.RequestHeader;
import com.github.javaparser.utils.Log;
import com.google.common.collect.Lists;
import com.yfj.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CheckLiveCheckService {

    public boolean traverseHotel(String supplierId, String distributorId) throws IOException {
        String hotelIds = "";
        String hotelString = doPost(Constants.URL_HOTEL, obtainParamHotel(supplierId, distributorId));
        JSONObject hotelIdJson = JSONObject.parseObject(hotelString);
        if (StringUtils.isNotEmpty(hotelString) && hotelIdJson.getString("result") != null) {
            hotelIds = hotelIdJson.getString("result");
        }
        log.info(hotelIds);
        if (StringUtils.isNotEmpty(hotelIds)) {
            JSONArray hotelArray = JSONObject.parseArray(hotelIds);
            log.info(hotelArray.toString());
            int limit=0;
            for (Object o : hotelArray) {
                String oldResponse = obtainOldArchitectureData(o.toString(), supplierId, distributorId);
                List<AvailRoomStayDTO> oldRoomStays = ObtainProductFromOld(oldResponse);
                List<AvailRoomStayDTO> newRoomStays = obtainNewArchitecture(o.toString());
                boolean equalFlag = isEquals(oldRoomStays, newRoomStays);
                if (!equalFlag) {
                    log.info("inconsistent results------------supplier---" + supplierId);
                    log.info("------------distributor---" + distributorId);
                    log.info("------------hotelId--" + o);
                    return false;
                }
               if (++limit>50){
                 break;
               }
            }
            log.info("------------------new and old architecture result is the same");
            return true;
        }
        log.info("----------------no hotelIds from" + supplierId + "----" + distributorId + "----------connectivity");
        return false;
    }


    private String obtainParamHotel(String supplierId, String distributorId) {
        JSONObject paramHotelJson = new JSONObject();
        paramHotelJson.put("method", "listHotelCodes");
        paramHotelJson.put("params", Lists.newArrayList(supplierId, distributorId));
        log.info(paramHotelJson.toString());
        return paramHotelJson.toString();
    }

    public String obtainOldArchitectureData(String hotelId, String supplierId, String distributorId) throws UnsupportedEncodingException {
        JSONObject paramJson = new JSONObject();
        paramJson.put("endpoint", "ccs!/singapore?topic=aws_router_endpoints");
        paramJson.put("sourceId", supplierId);
        paramJson.put("distributorId", distributorId);
        paramJson.put("hotelId", hotelId);
        paramJson.put("checkIn", "2022-07-19");
        paramJson.put("checkout", "2022-07-20");
        paramJson.put("roomCount", 1);
        paramJson.put("adultCount", 1);
        paramJson.put("childCount", 0);
        paramJson.put("childAges", List.of());
        paramJson.put("roomTypes", List.of());
        paramJson.put("ratePlans", List.of());
        paramJson.put("iata", null);
        paramJson.put("language", null);
        paramJson.put("country", null);
        paramJson.put("device", null);
        List<Object> paramList = new ArrayList<>();
        paramList.add(paramJson);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", "noCachedAvailability");
        jsonObject.put("params", paramList);
        log.info(jsonObject.toString());
        return doPost(Constants.URL_OLD, jsonObject.toString());

    }


    public boolean isEquals(List<AvailRoomStayDTO> list1, List<AvailRoomStayDTO> list2) {
        if (list1 == null && list2 == null) {
            return true;
        }
        //Only one of them is null
        else if (list1 == null || list2 == null) {
            return false;
        } else if (list1.size() != list2.size()) {
            return false;
        }
        return CollectionUtils.isEqualCollection(list1, list2);
    }


    private List<AvailRoomStayDTO> ObtainProductFromOld(String oldReponse) throws IOException {
        List<AvailRoomStayDTO> roomStayDTOS = new ArrayList<>();
        JSONObject oldReponseJson = JSON.parseObject(oldReponse);
        if (oldReponseJson != null && oldReponseJson.getString("result") != null) {
            String result = oldReponseJson.getString("result");
            JSONObject resultJson = JSON.parseObject(result);
            if (resultJson != null && resultJson.getString("hotelAvailRS") != null) {
                String hotelAvailRS = resultJson.getString("hotelAvailRS");
                roomStayDTOS = ObtainRoomStays(hotelAvailRS);
            }
        }
        return roomStayDTOS;
    }

    private List<AvailRoomStayDTO> ObtainRoomStays(String hotelAvailRS) {
        List<AvailRoomStayDTO> roomStayDTOS = new ArrayList<>();
        JSONObject hotelAvailRSJson = JSON.parseObject(hotelAvailRS);
        if (hotelAvailRSJson != null && hotelAvailRSJson.getString("hotelAvailRoomStays") != null) {
            String hotelAvailRoomStays = hotelAvailRSJson.getString("hotelAvailRoomStays");
            JSONArray hotelAvailRoomStaysArray = JSONArray.parseArray(hotelAvailRoomStays);
            if (hotelAvailRoomStaysArray != null && hotelAvailRoomStaysArray.get(0) != null) {
                String availRoomStay = hotelAvailRoomStaysArray.get(0).toString();
                JSONObject availRoomStayJson = JSON.parseObject(availRoomStay);
                if (availRoomStayJson != null && availRoomStayJson.getString("roomStays") != null) {
                    String roomStays = availRoomStayJson.getString("roomStays");
                    JSONArray roomStayArray = JSONObject.parseArray(roomStays);
                    /*  roomStayDTOS = JSON.parseArray(roomStayArray.toJSONString(), AvailRoomStayDTO.class);*/
                    for (int i = 0; i < roomStayArray.size(); i++) {
                        AvailRoomStayDTO availRoomStayDTO = JSONObject.parseObject(roomStayArray.get(0).toString(), AvailRoomStayDTO.class);
                        JSONObject roomRateJson = JSON.parseObject(roomStayArray.get(0).toString());
                        String roomRate = roomRateJson.getString("roomRate");
                        JSONObject ratesJson = JSON.parseObject(roomRate);
                        String rates = ratesJson.getString("rates");
                        JSONArray roomRateArray = JSONObject.parseArray(rates);
                        List<RateDTO> rateDTOS = JSONObject.parseArray(roomRateArray.toJSONString(), RateDTO.class);

                        availRoomStayDTO.getRoomRate().setRatesList(rateDTOS);
                        roomStayDTOS.add(availRoomStayDTO);
                    }
                    Log.info("----------------" + roomStayDTOS);
                }
            }

        }

        return roomStayDTOS;
    }

    public List<AvailRoomStayDTO> obtainNewArchitecture(String hotelId) {
        List<AvailRoomStayDTO> roomStays = new ArrayList<>();
        //derby!service-registry.derbysoft-test.com:10080!/public@router:us-west-2::::
        DefaultHotelBuyerRemoteService remoteService = new DefaultHotelBuyerRemoteService(Constants.URL_NEW);
        HotelAvailResponse response = remoteService.getNoCachedAvailability(initNewArchitectureRequest(hotelId));
        if (response.getError() != null
                || response.getHotelAvailRS() == null
                || CollectionUtils.isNotEmpty(response.getHotelAvailRS().getNoAvailHotelsList())
                || CollectionUtils.isEmpty(response.getHotelAvailRS().getHotelAvailRoomStaysList())
                || CollectionUtils.isEmpty(response.getHotelAvailRS().getHotelAvailRoomStaysList().get(0).getRoomStaysList())) {

        } else {
            roomStays = response.getHotelAvailRS().getHotelAvailRoomStaysList().get(0).getRoomStaysList();

        }
        log.info("----------------------------");
        String res = JSONObject.toJSONString(response);
        System.out.println(res);
        log.info("----------------------------");
        return roomStays;
    }


    private HotelAvailRequest initNewArchitectureRequest(String hotelId) {
        HotelAvailRequest hotelAvailRequest = new HotelAvailRequest();

        RequestHeader header = new RequestHeader();
        header.setSource("12306");
        header.setDestination("GOSANDBOX");
        header.setTaskId("187be58c62c2f2515b5d78ee");
        hotelAvailRequest.setHeader(header);

        HotelAvailCriteriaDTO criteriaDTO = new HotelAvailCriteriaDTO();

      /*  TPAExtensionsDTO tpaExtensionsDTO = new TPAExtensionsDTO();
        KeyValue keyValue = new KeyValue();
        keyValue.setKey(Constants.TpaExtensions.STAY_TYPE);
        keyValue.setValue("OverNightRoom");
        tpaExtensionsDTO.setElementsList(List.of(keyValue));
        criteriaDTO.setTpaExtensions(tpaExtensionsDTO);*/

        /*  criteriaDTO.setLanguage("test");*/
        criteriaDTO.setHotelCodesList(Lists.newArrayList(hotelId));

        StayDateRangeDTO stayDateRangeDTO = new StayDateRangeDTO();
        stayDateRangeDTO.setCheckin("2022-07-19");
        stayDateRangeDTO.setCheckout("2022-07-20");
        criteriaDTO.setStayDateRange(stayDateRangeDTO);

        RoomStayCandidateDTO roomStayCandidateDTO = new RoomStayCandidateDTO();
        roomStayCandidateDTO.setNumberOfUnits(1);
        roomStayCandidateDTO.setChildCount(2);
        roomStayCandidateDTO.setAdultCount(1);
        roomStayCandidateDTO.setChildAgesList(List.of(8, 10));
        criteriaDTO.setRoomStayCandidate(roomStayCandidateDTO);

      /*  criteriaDTO.setRoomTypesList(new ArrayList<String>());
        criteriaDTO.getRoomTypesList().add("test");
        criteriaDTO.getRoomTypesList().add("test1");
        criteriaDTO.setRatePlansList(new ArrayList<String>());
        criteriaDTO.getRatePlansList().add("test");
        criteriaDTO.getRatePlansList().add("test1");

        CorpAccount corpAccount = new CorpAccount("test", "test");
        criteriaDTO.setCorpAccount(corpAccount);*/

        HotelAvailRQ hotelAvailRQ = new HotelAvailRQ();
        hotelAvailRQ.setAvailCriteria(criteriaDTO);
        hotelAvailRequest.setHotelAvailRQ(hotelAvailRQ);
        return hotelAvailRequest;
    }


    public static String doPost(String url, String json) throws UnsupportedEncodingException {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setContentCharset("UTF-8");
        PostMethod postMethod = new PostMethod(url);
        RequestEntity entity = new StringRequestEntity(json, "application/json", "UTF-8");
        postMethod.setRequestEntity(entity);
        String returnStr = "";
        try {
            int code = httpClient.executeMethod(postMethod);
            if (code == 200) {
                InputStream in = postMethod.getResponseBodyAsStream();
                //下面将stream转换为String
                StringBuffer sb = new StringBuffer();
                InputStreamReader isr = new InputStreamReader(in, "UTF-8");
                char[] b = new char[4096];
                for (int n; (n = isr.read(b)) != -1; ) {
                    sb.append(new String(b, 0, n));
                }
                returnStr = sb.toString();
                System.out.println(returnStr);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnStr;
    }


}
