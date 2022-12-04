package com.predic8.membrane.core.openapi.validators;

import com.predic8.membrane.core.openapi.*;
import com.predic8.membrane.core.openapi.model.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static com.predic8.membrane.core.openapi.util.JsonUtil.*;
import static com.predic8.membrane.core.openapi.util.TestUtils.getResourceAsStream;
import static java.lang.Boolean.*;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;


public class ArrayTest {

    OpenAPIValidator validator;

    @Before
    public void setUp() {
        validator = new OpenAPIValidator(getResourceAsStream(this,"/openapi/array.yml"));
    }

    @Test
    public void noType() {

        Map<String,Object> m = new HashMap<>();
        m.put("no-type", listWithDifferentTypes());

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void onlyNumbersValid() {

        List<Number> l = new ArrayList<>();
        l.add(7);
        l.add(0.5);
        l.add(10000000);

        Map<String,List<Number>> m = new HashMap<>();
        m.put("only-numbers", l);

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        assertEquals(0,errors.size());
    }

    @Test
    public void onlyNumbersInvalid() {

        Map<String , Object> m = new HashMap<>();
        m.put("only-numbers", listWithDifferentTypes());

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(2,errors.size());
    }

// As of 2022-11-20 parser does not support OpenAPI 3.1.0
//    @Test
//    public void prefixedInvalid() {
//
//        List l = Arrays.asList("foo","DE",7,true);
//
//        Map m = new HashMap();
//        m.put("prefixed", l);
//
//        System.out.println("m = " + m);
//
//        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
//        assertEquals(2,errors.size());
//    }

    @Test
    public void minMaxValid() {

        Map<String,Object> m = new HashMap<>();
        m.put("min-max", Arrays.asList("foo",7));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void minMaxTooLessInvalid() {

        Map<String,List<String>> m = new HashMap<>();
        m.put("min-max", singletonList("foo"));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/min-max",e.getContext().getJSONpointer());
        assertTrue(e.getMessage().contains("minItems"));
        assertEquals("REQUEST/BODY/min-max", e.getContext().getLocationForRequest());
    }

    @Test
    public void minMaxTooManyInvalid() {

        Map<String,List<Object>> m = new HashMap<>();
        m.put("min-max", Arrays.asList("foo",7,true,8,"bar"));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/min-max",e.getContext().getJSONpointer());
        assertTrue(e.getMessage().contains("maxItems"));
        assertEquals("REQUEST/BODY/min-max", e.getContext().getLocationForRequest());
    }


    @Test
    public void uniqueItemsInvalid() {

        Map<String,List<Integer>> m = new HashMap<>();
        m.put("uniqueItems", Arrays.asList(4,5,2,3,9,1,2,0));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/uniqueItems",e.getContext().getJSONpointer());
        assertTrue(e.getMessage().contains("2"));
        assertEquals("REQUEST/BODY/uniqueItems", e.getContext().getLocationForRequest());
    }

    @Test
    public void uniqueItemsValid() {

        Map<String,List<Integer>> m = new HashMap<>();
        m.put("uniqueItems", Arrays.asList(4,5,2,3,9,1,0));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
        assertEquals(0,errors.size());
    }

    @Test
    public void validateObjectInArrayValid() {

        Map<String,Object> o1 = new HashMap<>();
        o1.put("a","foo");
        o1.put("b",7);

        Map<String,Object> o2 = new HashMap<>();
        o2.put("a","baz");
        o2.put("b",3);

        Map<String,Object> o3 = new HashMap<>();
        o3.put("a","bar");
        o3.put("b",11);

        Map<String,Object> m = new HashMap<>();
        m.put("objects", Arrays.asList(o1,o2,o3));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void validateObjectInArrayInvalid() {

        Map<String,Object> o1 = new HashMap<>();
        o1.put("a","foo");
        o1.put("b",7);

        Map<String,String> o2 = new HashMap<>();
        o2.put("a","baz");

        Map<String,Object> o3 = new HashMap<>();
        o3.put("a","bar");
        o3.put("b",11);

        Map<String,Object> m = new HashMap<>();
        m.put("objects", Arrays.asList(o1,o2,o3));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/objects/1/b",e.getContext().getJSONpointer());
        assertTrue(e.getMessage().contains("missing"));
        assertEquals("REQUEST/BODY/objects/1/b", e.getContext().getLocationForRequest());

    }

    private List<Object> listWithDifferentTypes() {
        List<Object> l = new ArrayList<>();
        l.add(7);
        l.add("foo");
        l.add(TRUE);
        return l;
    }
}