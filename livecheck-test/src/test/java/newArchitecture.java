import com.alibaba.fastjson.JSONObject;
import com.derbysoft.dswitch.dto.hotel.avail.HotelAvailCriteriaDTO;
import com.derbysoft.dswitch.dto.hotel.avail.HotelAvailRQ;
import com.derbysoft.dswitch.dto.hotel.avail.RoomStayCandidateDTO;
import com.derbysoft.dswitch.dto.hotel.common.StayDateRangeDTO;
import com.derbysoft.dswitch.remote.hotel.buyer.DefaultHotelBuyerRemoteService;
import com.derbysoft.dswitch.remote.hotel.dto.HotelAvailRequest;
import com.derbysoft.dswitch.remote.hotel.dto.HotelAvailResponse;
import com.derbysoft.dswitch.remote.hotel.dto.RequestHeader;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

@Slf4j
public class newArchitecture {

    @Test
    public void checkData(){
        //derby!service-registry.derbysoft-test.com:10080!/public@router:us-west-2::::
        DefaultHotelBuyerRemoteService remoteService = new DefaultHotelBuyerRemoteService("34.217.218.157:9002");
        HotelAvailResponse response =remoteService.getNoCachedAvailability(initRequest());
        log.info("----------------------------");
        String res = JSONObject.toJSONString(response);
       /* String res=printXml(response);*/
        System.out.println(res);
        log.info("----------------------------");
    }


    private HotelAvailRequest initRequest() {
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
        criteriaDTO.setHotelCodesList(Lists.newArrayList("GO101"));

        StayDateRangeDTO stayDateRangeDTO = new StayDateRangeDTO();
        stayDateRangeDTO.setCheckin("2022-07-20");
        stayDateRangeDTO.setCheckout("2022-07-24");
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
}
