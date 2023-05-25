package com.xingray.java.server.spring.mvc.param;

import com.xingray.java.json.ClassNode;
import com.xingray.java.json.JsonConverter;
import com.xingray.java.util.ObjectUtil;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.MethodParameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class CustomHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver, SmartInitializingSingleton {

    public static final String DEFAULT_DATA_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;
    private final JsonConverter jsonConverter;

    public CustomHandlerMethodArgumentResolver(RequestMappingHandlerAdapter requestMappingHandlerAdapter,
                                               JsonConverter jsonConverter) {
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        resolvers.add(this);
        if (argumentResolvers != null && !argumentResolvers.isEmpty()) {
            resolvers.addAll(argumentResolvers);
        }
        requestMappingHandlerAdapter.setArgumentResolvers(resolvers);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CustomParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Map<String, String[]> parameterMap = webRequest.getParameterMap();
        Parameter parameter1 = parameter.getParameter();
        Type parameterizedType = parameter1.getParameterizedType();
        if (!(parameterizedType instanceof Class<?>)) {
            throw new IllegalStateException("unknown type:" + parameterizedType);
        }

        Class paramClass = (Class) parameterizedType;
        Constructor constructor = paramClass.getConstructor(null);
        Object paramObject = constructor.newInstance(null);

        Field[] declaredFields = paramClass.getDeclaredFields();
        for (Field field : declaredFields) {
            String key;
            String defaultValue;
            boolean isRequire = false;

            CustomParamField requestParam = field.getAnnotation(CustomParamField.class);
            if (requestParam == null) {
                key = field.getName();
                defaultValue = null;
            } else {
                key = requestParam.value();
                if (key == null) {
                    key = requestParam.name();
                }
                defaultValue = requestParam.defaultValue();
                if (defaultValue.equals(ValueConstants.DEFAULT_NONE)) {
                    defaultValue = null;
                }
                isRequire = requestParam.required();
            }

            String[] values = parameterMap.get(key);
            setFieldValue(paramObject, field, isRequire, values, defaultValue);
        }

        return paramObject;

    }

    private void setFieldValue(Object paramObject, Field field, boolean isRequire, String[] values, String defaultValue) throws Exception {
        if (values == null) {
            if (defaultValue != null) {
                // 设置默认值
                setValue(paramObject, field, defaultValue.split("&"));
            } else {
                if (isRequire) {
                    throw new IllegalArgumentException("field :" + field.getName() + " is required, but not passed");
                }
            }
            return;
        }

        setValue(paramObject, field, values);
    }

    private void setValue(Object paramObject, Field field, String[] values) throws Exception {
        if (values == null) {
            return;
        }
        Class<?> fieldType = field.getType();
        if (fieldType.isArray()) {
            ObjectUtil.setArrayValue(paramObject, field, values, fieldType);
        } else if (List.class.isAssignableFrom(fieldType)) {
            ObjectUtil.setListValue(paramObject, field, values, fieldType);
        }
        if (values.length > 1) {
            throw new IllegalArgumentException("field [" + field.getName() + "] is not array or list, fieldType:" + fieldType.getName());
        }
        if (values.length == 0) {
            return;
        }
        String value = values[0];
        Object fieldValue = getFieldValue(field, value);
        if (fieldValue != null) {
            field.setAccessible(true);
            field.set(paramObject, fieldValue);
        }
    }

    private Object getFieldValue(Field field, String value) throws Exception {
        Class<?> fieldType = field.getType();
        if (fieldType.equals(BigDecimal.class)) {
            return new BigDecimal(value);
        }

        if (fieldType.equals(Date.class)) {
            DateTimeFormat dateTimeFormat = field.getAnnotation(DateTimeFormat.class);
            String pattern;
            if (dateTimeFormat == null) {
                pattern = DEFAULT_DATA_FORMAT_PATTERN;
            } else {
                pattern = dateTimeFormat.pattern();
            }
            return new SimpleDateFormat(pattern).parse(value);
        }

        JsonObjectField annotation = field.getAnnotation(JsonObjectField.class);
        if (annotation != null) {
            return jsonConverter.toObject(value, fieldType);
        }
        JsonArrayField jsonArrayField = field.getAnnotation(JsonArrayField.class);
        if (jsonArrayField != null) {
            Class<?> elementClass = jsonArrayField.value();
            if (fieldType.equals(List.class)) {
                return jsonConverter.toObject(value, new ClassNode<>(List.class, elementClass));
            } else if (List.class.isAssignableFrom(fieldType)) {
                List list = (List) fieldType.getDeclaredConstructor(null).newInstance(null);
                list.addAll(jsonConverter.toObject(value, new ClassNode<>(List.class, elementClass)));
                return list;
            } else if (fieldType.isArray()) {
                List list = jsonConverter.toObject(value, new ClassNode<>(List.class, elementClass));
                int size = list.size();
                Object targetArray = Array.newInstance(elementClass, size);
                for (int i = 0; i < size; i++) {
                    Array.set(targetArray, i, list.get(i));
                }
            }
        }

        return ObjectUtil.ensureMatchesType(value, fieldType);
    }
}
