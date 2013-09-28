package cn.nit.beauty.request;

import cn.nit.beauty.model.Person;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(parameters, headers);

        RestTemplate restTemplate = getRestTemplate();
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

        return restTemplate.postForObject("http://192.168.1.102:8080/beauty-ajax/api/register", request, Person.class);
    }
}
