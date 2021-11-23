
package com.lzy.imagepickerdemo;

import com.lzy.imagepicker.ImagePicker;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.graphics.Surface;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.Context;
import ohos.app.Environment;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.CameraConfig;
import ohos.media.camera.device.CameraStateCallback;
import ohos.media.camera.device.FrameConfig;
import ohos.media.image.ImageReceiver;
import ohos.media.image.common.ImageFormat;
import ohos.security.SystemPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static ohos.bundle.IBundleManager.PERMISSION_GRANTED;
import static ohos.media.camera.device.Camera.FrameConfigType.FRAME_CONFIG_PICTURE;
import static ohos.media.camera.device.Camera.FrameConfigType.FRAME_CONFIG_PREVIEW;

/**
 * Camera CameraAbility
 *
 * @since 2020-01-17
 */
public class CameraAbility extends Ability {
    private static String TAG = "MainAbility - CameraAbility";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int SCREEN_WIDTH = 1080;

    private static final int SCREEN_HEIGHT = 2340;

    private static final int IMAGE_RCV_CAPACITY = 5;

    private static final String IMG_FILE_PREFIX = "IMG_";

    private static final String IMG_FILE_TYPE = ".jpg";

    private Surface previewSurface;

    private SurfaceProvider surfaceProvider;

    private boolean isCameraRear;

    private Camera cameraDevice;

    private EventHandler creamEventHandler;

    private ImageReceiver imageReceiver;


    private File targetFile;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_camera);
        requestCameraPermission();
        getWindow().setTransparent(true);
        initSurface();
        initControlComponents();

        creamEventHandler = new EventHandler(EventRunner.create("CameraBackground"));
    }


    private void initSurface() {
        surfaceProvider = new SurfaceProvider(this);
        DirectionalLayout.LayoutConfig params = new DirectionalLayout.LayoutConfig(
                ComponentContainer.LayoutConfig.MATCH_PARENT, ComponentContainer.LayoutConfig.MATCH_PARENT);

        surfaceProvider.setLayoutConfig(params);
        surfaceProvider.pinToZTop(false);

        surfaceProvider.getSurfaceOps().get().addCallback(new SurfaceCallBack());

        ((ComponentContainer) findComponentById(ResourceTable.Id_surface_container)).addComponent(surfaceProvider);
    }

    private void initControlComponents() {
        findComponentById(ResourceTable.Id_tack_picture_btn).setClickedListener(this::takePicture);
        findComponentById(ResourceTable.Id_exit).setClickedListener(component -> terminate());
        findComponentById(ResourceTable.Id_switch_camera_btn).setClickedListener(component -> switchClicked());
    }

    private void terminate() {

        Intent intent = new Intent();
        ImagePicker imagePicker = ImagePicker.getInstance();
        if (targetFile != null) {
            imagePicker.setTakeImageFile(targetFile);
//            intent.setParam(ImagePicker.EXTRA_TARGET_FILE_PATH, targetFile.getAbsolutePath());
            setResult(ImagePicker.RESULT_OK, intent);
        } else {
//            intent.setParam(ImagePicker.EXTRA_TARGET_FILE_PATH, "");
            setResult(ImagePicker.RESULT_CANCELED, intent);
        }
        terminateAbility();
    }

    private void takePicture(Component con) {
        FrameConfig.Builder framePictureConfigBuilder = cameraDevice.getFrameConfigBuilder(FRAME_CONFIG_PICTURE);
        framePictureConfigBuilder.addSurface(imageReceiver.getRecevingSurface());
        FrameConfig pictureFrameConfig = framePictureConfigBuilder.build();
        cameraDevice.triggerSingleCapture(pictureFrameConfig);
    }

    private void switchClicked() {
        isCameraRear = !isCameraRear;
        openCamera();
    }

    @Override
    protected void onStop() {

        releaseCamera();
    }

    private void openCamera() {

        imageReceiver = ImageReceiver.create(SCREEN_WIDTH, SCREEN_HEIGHT, ImageFormat.JPEG, IMAGE_RCV_CAPACITY);
        imageReceiver.setImageArrivalListener(this::saveImage);

        CameraKit cameraKit = CameraKit.getInstance(getApplicationContext());
        String[] cameraList = cameraKit.getCameraIds();
        String cameraId = cameraList.length > 1 && isCameraRear ? cameraList[1] : cameraList[0];
        CameraStateCallbackImpl cameraStateCallback = new CameraStateCallbackImpl();
        cameraKit.createCamera(cameraId, cameraStateCallback, creamEventHandler);
    }

    private void saveImage(ImageReceiver receiver) {
        String fileName = "photo" + System.currentTimeMillis() + IMG_FILE_TYPE;
        targetFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        ohos.media.image.Image.Component component =
                receiver.readNextImage().getComponent(ImageFormat.ComponentType.JPEG);
        byte[] bytes = new byte[component.remaining()];
        component.read(bytes);

        try (FileOutputStream output = new FileOutputStream(targetFile)) {
            output.write(bytes);
            showTips(CameraAbility.this, "Take picture success,save path:" + targetFile.getAbsolutePath());
        } catch (IOException e) {
            showTips(CameraAbility.this, "error");
        }
    }

    private void releaseCamera() {
        if (cameraDevice != null) {
            cameraDevice.release();
            cameraDevice = null;
        }

        if (imageReceiver != null) {
            imageReceiver.release();
            imageReceiver = null;
        }

        if (creamEventHandler != null) {
            creamEventHandler.removeAllEvent();
            creamEventHandler = null;
        }
    }

    private void showTips(Context context, String message) {
        getUITaskDispatcher().asyncDispatch(() -> {
            ToastDialog toastDialog = new ToastDialog(context);
            toastDialog.setAutoClosable(false);
            toastDialog.setContentText(message);
            toastDialog.show();
        });
    }

    @Override
    protected void onBackPressed() {
        super.onBackPressed();
        terminate();
    }

    /**
     * CameraStateCallbackImpl
     *
     * @since 2020-09-03
     */
    class CameraStateCallbackImpl extends CameraStateCallback {
        CameraStateCallbackImpl() {
        }

        @Override
        public void onCreated(Camera camera) {
            previewSurface = surfaceProvider.getSurfaceOps().get().getSurface();
            if (previewSurface == null) {

                return;
            }
            if (imageReceiver == null) {

                return;
            }

            // Wait until the preview surface is created.
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                showTips(CameraAbility.this,"error");
            }

            CameraConfig.Builder cameraConfigBuilder = camera.getCameraConfigBuilder();
            cameraConfigBuilder.addSurface(previewSurface);
            cameraConfigBuilder.addSurface(imageReceiver.getRecevingSurface());

            camera.configure(cameraConfigBuilder.build());
            cameraDevice = camera;
        }

        @Override
        public void onConfigured(Camera camera) {
            FrameConfig.Builder framePreviewConfigBuilder = camera.getFrameConfigBuilder(FRAME_CONFIG_PREVIEW);
            framePreviewConfigBuilder.addSurface(previewSurface);
            camera.triggerLoopingCapture(framePreviewConfigBuilder.build());
        }
    }

    /**
     * SurfaceCallBack
     *
     * @since 2020-09-03
     */
    class SurfaceCallBack implements SurfaceOps.Callback {
        @Override
        public void surfaceCreated(SurfaceOps callbackSurfaceOps) {
            if (callbackSurfaceOps != null) {
                callbackSurfaceOps.setFixedSize(SCREEN_HEIGHT, SCREEN_WIDTH);
            }
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceOps callbackSurfaceOps, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceOps callbackSurfaceOps) {
        }
    }

    private void requestCameraPermission() {
        List<String> permissions = new LinkedList<String>(Arrays.asList(SystemPermission.WRITE_USER_STORAGE,
                SystemPermission.READ_USER_STORAGE, SystemPermission.CAMERA));
        permissions.removeIf(
                permission -> verifySelfPermission(permission) == PERMISSION_GRANTED || !canRequestPermission(permission));

        if (!permissions.isEmpty()) {
            requestPermissionsFromUser(permissions.toArray(new String[permissions.size()]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsFromUserResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return;
        }
        for (int grantResult : grantResults) {
            if (grantResult != PERMISSION_GRANTED) {
                terminateAbility();
                return;
            }
        }
        restart();
    }
}
