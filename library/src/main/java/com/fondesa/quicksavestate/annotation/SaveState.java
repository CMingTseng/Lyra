package com.fondesa.quicksavestate.annotation;

import com.fondesa.quicksavestate.coder.StateCoder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by antoniolig on 17/02/17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SaveState {
    Class<? extends StateCoder> value() default StateCoder.class;
}