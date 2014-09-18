package cn.nit.beauty.request;

import cn.nit.beauty.model.Person;
import cn.nit.beauty.utils.Data;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/**
 * Created by Administrator on 13-7-24.
 */
public class RegisterRequest extends SpringAndroidSpiceRequest<Person> {
    Person person;

    public RegisterRequest(Person person) {
        super(Person.class);
        this.person = person;
    }

    @Override
    public Person loadDataFromNetwork() throws Exception {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
        parameters.set("username", person.getUsername());
        parameters.set("passwd", person.getPasswd());
        parameters.set("email", person.getEmail());
        parameters.set("phone", person.getPhone());
        parameters.set("logintype", person.getLogintype());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(parameters, headers);

        RestTemplate restTemplate = getRestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        return restTemplate.postForObject(Data.AUTH_URL + "/register", request, Person.class);
    }
}
