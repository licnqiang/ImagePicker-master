package com.lzy.imagepickerdemo.utils;

import ohos.agp.colors.RgbColor;
import ohos.agp.components.Component;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.components.element.VectorElement;
import ohos.agp.render.Arc;
import ohos.agp.utils.Color;
import ohos.app.Context;
import ohos.global.resource.*;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;


public class ResUtil {
    private static final String TAG = "ResUtil";

    private static final String CITY_ID_ATTR = "cityId_";

    private static final String STRING_ID_ATTR = "String_";

    private ResUtil() {
    }

    /**
     * get the path from id
     *
     * @param context the context
     * @param id      the id
     * @return the path from id
     */
    public static String getPathById(Context context, int id) {
        String path = "";
        if (context == null) {
            return path;
        }
        ResourceManager manager = context.getResourceManager();
        if (manager == null) {
            return path;
        }
        try {
            path = manager.getMediaPath(id);
        } catch (IOException | NotExistException | WrongTypeException e) {
            return "";
        }
        return path;
    }

    /**
     * get the new color
     *
     * @param context the context
     * @param id      the id
     * @return the color
     */
    public static Color getNewColor(Context context, int id) {
        Color result = new Color(getColor(context, id));
        return result;
    }

    /**
     * get the color
     *
     * @param context the context
     * @param id      the id
     * @return the color
     */
    public static int getColor(Context context, int id) {
        int result = 0;
        if (context == null) {
            return result;
        }
        ResourceManager manager = context.getResourceManager();
        if (manager == null) {
            return result;
        }
        try {
            result = manager.getElement(id).getColor();
        } catch (IOException | NotExistException | WrongTypeException e) {
            return result;
        }
        return result;
    }

    /**
     * get the dimen value
     *
     * @param context the context
     * @param id      the id
     * @return get the float dimen value
     */
    public static float getDimen(Context context, int id) {
        float result = 0;
        if (context == null) {
            return result;
        }
        ResourceManager manager = context.getResourceManager();
        if (manager == null) {
            return result;
        }
        try {
            result = manager.getElement(id).getFloat();
        } catch (IOException | NotExistException | WrongTypeException e) {
            return result;
        }
        return result;
    }

    /**
     * get the dimen value
     *
     * @param context the context
     * @param id      the id
     * @return get the int dimen value
     */
    public static int getIntDimen(Context context, int id) {
        float value = getDimen(context, id);
        return (int) (value >= 0 ? value + 0.5f : value - 0.5f);
    }

    /**
     * get string
     *
     * @param context the context
     * @param id      the string id
     * @return string of the given id
     */
    public static String getString(Context context, int id) {
        String result = "";
        if (context == null) {
            return result;
        }
        ResourceManager manager = context.getResourceManager();
        if (manager == null) {
            return result;
        }
        try {
            result = manager.getElement(id).getString();
        } catch (IOException | WrongTypeException | NotExistException e) {
            return result;
        }
        return result;
    }

    /**
     * get boolean
     *
     * @param context the context
     * @param id      the boolean id
     * @return boolean of the given id
     */
    public static boolean getBoolean(Context context, int id) {
        boolean result = false;
        if (context == null) {
            return result;
        }
        ResourceManager manager = context.getResourceManager();
        if (manager == null) {
            return result;
        }
        try {
            result = manager.getElement(id).getBoolean();
        } catch (IOException | NotExistException | WrongTypeException e) {
            return result;
        }
        return result;
    }

    /* get the string array
     *
     * @param context the context
     * @param id the string array id
     * @return the string array
     */
    public static String[] getStringArray(Context context, int id) {
        String[] result = null;
        if (context == null) {
            return result;
        }
        ResourceManager manager = context.getResourceManager();
        if (manager == null) {
            return result;
        }
        try {
            result = manager.getElement(id).getStringArray();
        } catch (IOException | NotExistException | WrongTypeException e) {
            return result;
        }
        return result;
    }

    /**
     * get the int array
     *
     * @param context the context
     * @param id      the int array
     * @return the int array
     */
    public static int[] getIntArray(Context context, int id) {
        int[] result = null;
        if (context == null) {
            return result;
        }
        ResourceManager manager = context.getResourceManager();
        if (manager == null) {
            return result;
        }
        try {
            result = manager.getElement(id).getIntArray();
        } catch (IOException | NotExistException | WrongTypeException e) {
            return result;
        }
        return result;
    }

    /**
     * get the vector drawable
     *
     * @param context the context
     * @param id      the drawable id
     * @return the vector drawable
     */
    public static VectorElement getVectorDrawable(Context context, int id) {
        if (context == null) {
            return null;
        }
        return new VectorElement(context, id);
    }

    /**
     * get the pixel map
     *
     * @param context the context
     * @param id      the id
     * @return the pixel map
     */
    public static Optional<PixelMap> getPixelMap(Context context, int id) {
        String path = getPathById(context, id);
        if (path == null || path.length() == 0) {
            return Optional.empty();
        }
        RawFileEntry assetManager = context.getResourceManager().getRawFileEntry(path);
        ImageSource.SourceOptions options = new ImageSource.SourceOptions();
        options.formatHint = "image/png";
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        try {
            Resource asset = assetManager.openRawFile();
            ImageSource source = ImageSource.create(asset, options);
            return Optional.ofNullable(source.createPixelmap(decodingOptions));
        } catch (IOException e) {
            return Optional.empty();
        }

    }

    /**
     * get the Pixel Map Element
     *
     * @param context the context
     * @param resId   the res id
     * @return the Pixel Map Element
     */
    public static PixelMapElement getPixelMapDrawable(Context context, int resId) {
        Optional<PixelMap> optional = getPixelMap(context, resId);
        return optional.map(PixelMapElement::new).orElse(null);
    }

    public static Element getColorDrawable(int color) {
        ShapeElement drawable = new ShapeElement();
        drawable.setShape(ShapeElement.RECTANGLE);
        drawable.setRgbColor(RgbColor.fromArgbInt(color));
        return drawable;
    }


    /**
     * get the Element
     *
     * @param color the color
     * @return the Element
     */
    public static Element buildDrawableByColor(int color) {
        ShapeElement drawable = new ShapeElement();
        drawable.setShape(ShapeElement.RECTANGLE);
        drawable.setRgbColor(RgbColor.fromArgbInt(color));
        return drawable;
    }

    /**
     * get the Element By ColorRadius
     *
     * @param color  the color
     * @param radius the radius
     * @return the Element By ColorRadius
     */
    public static Element buildDrawableByColorRadius(int color, float radius) {
        ShapeElement drawable = new ShapeElement();
        drawable.setShape(ShapeElement.RECTANGLE);
        drawable.setRgbColor(RgbColor.fromArgbInt(color));
        drawable.setCornerRadius(radius);
        return drawable;
    }

    /**
     * get the ShapeElement
     *
     * @param thickness  the thickness
     * @param inside     the inside color
     * @param border     the border color
     * @param startAngle the start angle
     * @param sweepAngle the sweep angle
     * @return the ShapeElement
     */
    public static ShapeElement getCustomArcGradientDrawable(int thickness, Color inside, Color border, float startAngle,
                                                            float sweepAngle) {
        ShapeElement drawable = new ShapeElement();
        drawable.setShape(ShapeElement.ARC);
        drawable.setRgbColor(RgbColor.fromArgbInt(inside.getValue())); // keep it transparent for main(inner) part
        drawable.setArc(new Arc(startAngle, sweepAngle, false));
        drawable.setStroke(thickness, RgbColor.fromArgbInt(border.getValue()));
        return drawable;
    }

    /**
     * get res id by reflect
     *
     * @param resClass res class
     * @param resName  res name
     * @return res id
     */
    public static OptionalInt getResIdByReflect(Class resClass, String resName) {
        return null;
    }


    /**
     * get native city name
     *
     * @param context the context
     * @param cityId  cityId
     * @return city name from cityId
     */
    public static String getNativeCityName(Context context, String cityId) {
        return null;
    }

    /**
     * find view by id
     *
     * @param view rootView
     * @param id   res id
     * @param <T>  type
     * @return view
     */
    public static <T extends Component> T findViewById(Component view, int id) {
        if (view == null) {
            return null;
        }
        return (T) view.findComponentById(id);
    }
}

