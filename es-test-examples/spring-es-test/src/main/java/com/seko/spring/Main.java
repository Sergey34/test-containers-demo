package com.seko.spring;

import com.seko.spring.repository.EsRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.seko.spring");
        EsRepository esRepository = context.getBean("esRepository", EsRepository.class);
        Map<String, Object> doc = new HashMap<>();
        doc.put("field_1", "value_1");
        doc.put("field_2", "value_2");
        doc.put("field_3", "value_3");
        esRepository.createDoc(doc, "1");
    }
}
