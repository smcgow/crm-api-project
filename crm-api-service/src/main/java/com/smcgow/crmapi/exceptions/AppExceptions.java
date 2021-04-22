package com.smcgow.crmapi.exceptions;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class AppExceptions {

    /***
     *
     * @param args
     */
    @SneakyThrows
    public static void throwInvalidBasicAuthCredentialsException(Object... args) {
        throwException(InvalidBasicAuthCredentialsException.class,args);
    }

    @SneakyThrows
    public static void throwFailedAutheniticateRequestException(Object... args) {
        throwException(FailedAutheniticateRequestException.class,args);
    }

    @SneakyThrows
    public static void throwJsonResponseAbsentException(Object... args) {
        throwException(JsonResponseAbsentException.class,args);
    }

    @SneakyThrows
    public static void throwInvocationOfServiceFailedException(Object... args) {
        throwException(InvocationOfServiceFailedException.class,args);
    }

    @SneakyThrows
    public static void throwJsonMessageParsingException(Object... args) {
        throwException(JsonMessageParsingException.class,args);
    }

    @SneakyThrows
    public static void throwCrmApplicationConfigurationException(Object... args) {
        throwException(CrmApplicationConfigurationException.class,args);
    }

    /**
     * All excepptions have three overriden constructors. This factory picks the appropriate constructor
     * based on the var-args present. either a message, a message and a throwable otr a throwable just.
     * Above are the individual factory calls for creating those exception types.
     * @param exceptionClass
     * @param args
     * @throws Throwable
     */
    @SneakyThrows(NoSuchMethodException.class)
    private static void throwException(Class exceptionClass, Object... args) throws Throwable {

        List<Constructor> constructors = Arrays.asList(exceptionClass.getConstructors());
        if(args.length == 1 && args[0] instanceof String){
            Constructor constructor = exceptionClass.getConstructor(args[0].getClass());
            Object object = constructor.newInstance(args[0]);
            log.error((String) args[0]);
            throw (Throwable) object;
        }else if(args.length == 2){
            Constructor constructor = exceptionClass.getConstructor(args[0].getClass(),Throwable.class);
            Object object = constructor.newInstance(args[0],args[1]);
            log.error((String) args[0],args[1]);
            throw (Throwable) object;
        }else if(args.length == 1){
            Constructor constructor = exceptionClass.getConstructor(Throwable.class);
            Object object = constructor.newInstance(args[0]);
            log.error(((Throwable)args[0]).getMessage(),args[0]);
            throw (Throwable) object;
        }else{
            throw new InvocationOfServiceFailedException("Exception handling failed completely, see logs for details.");
        }
    }



}
