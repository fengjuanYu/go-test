import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
@Slf4j
public class OldArchitecture {


    public static void doPost(String url, String json) throws UnsupportedEncodingException {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setContentCharset("UTF-8");
        PostMethod postMethod = new PostMethod(url);
        RequestEntity entity=new StringRequestEntity(json,"application/json","UTF-8");
        postMethod.setRequestEntity(entity);
        try {
            int code = httpClient.executeMethod(postMethod);
            if (code == 200){
                InputStream in = postMethod.getResponseBodyAsStream();
                //下面将stream转换为String
                StringBuffer sb = new StringBuffer();
                InputStreamReader isr = new InputStreamReader(in, "UTF-8");
                char[] b = new char[4096];
                for(int n; (n = isr.read(b)) != -1;) {
                    sb.append(new String(b, 0, n));
                }
                String returnStr = sb.toString();
                System.out.println(returnStr);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void checkData() throws UnsupportedEncodingException {
        String url="http://skybound.derbysoftsec.com/gagent/redirect.ci?Redirect-URL=http://internal-local-nuke-go-2126578082.ap-southeast-1.elb.amazonaws.com/gagent/dswitch.rpc";
        String params="{ \"method\": \"noCachedAvailability\", \"params\": [ { \"endpoint\": \"ccs!/singapore?topic=aws_router_endpoints\", \"sourceId\": \"ATOUR\", \"distributorId\": \"AGODA\", \"hotelId\": \"1100026\", \"checkIn\": \"2022-07-11\", \"checkout\": \"2022-07-12\", \"roomCount\": 1, \"adultCount\": 1, \"childCount\": 0, \"childAges\": [], \"roomTypes\": [], \"ratePlans\": [], \"iata\": null, \"language\": null, \"country\": null, \"device\": null } ] }";
        doPost(url,params);

    }

}
