package com.lzy.imagepicker.util;

import ohos.agp.animation.AnimatorProperty;
import ohos.agp.components.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnimatorUtil {



    public static void fadeOut(Component component) {
        AnimatorProperty anim = component.createAnimatorProperty();
        try {
            Method alphaFrom = AnimatorProperty.class.getDeclaredMethod("alphaFrom", float.class);
            alphaFrom.setAccessible(true);
            alphaFrom.invoke(anim, 1);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            anim.alphaFrom(component.getAlpha());
        }
        anim.alpha(0).setDuration(400);
        anim.start();
    }

    public static void fadeIn(Component component) {
        AnimatorProperty anim = component.createAnimatorProperty();
        try {
            Method alphaFrom = AnimatorProperty.class.getDeclaredMethod("alphaFrom", float.class);
            alphaFrom.setAccessible(true);
            alphaFrom.invoke(anim, 0);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            anim.alphaFrom(0);
        }
        anim.alpha(1).setDuration(400);
        anim.start();
    }


    public static void topOut(Component component) {
        AnimatorProperty anim = component.createAnimatorProperty();
        anim.moveFromY(0).moveToY(-component.getHeight()).setDuration(400);
        anim.start();
    }

    public static void topIn(Component component) {
        AnimatorProperty anim = component.createAnimatorProperty();
        anim.moveFromY(-component.getHeight()).moveToY(0).setDuration(400);
        anim.start();
    }


}
