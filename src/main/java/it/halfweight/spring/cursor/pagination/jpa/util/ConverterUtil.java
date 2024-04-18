package it.halfweight.spring.cursor.pagination.jpa.util;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;

import java.util.Map;

public class ConverterUtil {

    private static ConvertUtilsBean convertUtilsBean;

    private ConverterUtil(){}


    public static void config(Map<Class<?>, org.apache.commons.beanutils.Converter> converterMap){
        convertUtilsBean = BeanUtilsBean.getInstance().getConvertUtils();
        convertUtilsBean.deregister();
        converterMap.forEach((aClass, converter) -> convertUtilsBean.register(converter,aClass));
    }
    /**
     * Convert the specified value to an object of the specified class (if
     * possible).  Otherwise, return a String representation of the value.
     *
     * @param value Value to be converted (may be null)
     * @param clazz Java class to be converted to (must not be null)
     * @return The converted value
     *
     * @throws ConversionException if thrown by an underlying Converter
     */
    public static Object convert(final Object value, final Class<?> clazz) {
        if(convertUtilsBean == null){
            return ConvertUtils.convert(value, clazz);
        }else {
            return convertUtilsBean.convert(value,clazz);
        }
    }
}
