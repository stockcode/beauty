package cn.nit.beauty.request;

import cn.nit.beauty.model.ImageInfos;
import cn.nit.beauty.model.Person;
import cn.nit.beauty.utils.Data;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Collections;

/**
 * Created by Administrator on 13-7-24.
 */
public class LoginRequest extends SpringAndroidSpiceRequest<Person> {
    Person person;

    public LoginRequest(Person person) {
        super(Person.class);
        this.person = person;
    }

    @Override
    public Person loadDataFromNetwork() throws Exception {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
        parameters.set("username", person.getUsername());
        parameters.set("passwd", person.getPasswd());
        parameters.set("nickname", person.getNickname());
        parameters.set("logintype", person.getLogintype());

        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8"));

        headers.setContentType(mediaType);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(parameters, headers);

        RestTemplate restTemplate = getRestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        return restTemplate.postForObject(Data.AUTH_URL + "/login", request, Person.class);
    }
}
