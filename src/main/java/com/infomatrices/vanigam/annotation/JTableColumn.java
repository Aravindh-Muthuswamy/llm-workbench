/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.annotation;
import java.lang.annotation.*;

/**
 *
 * @author aravindhmuthuswamy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JTableColumn {
    String name();
    String customer() default "";
    String supplier() default "";
    int order() default 0;
    boolean editable() default true;
}