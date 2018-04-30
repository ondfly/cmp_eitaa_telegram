package ir.eitaa.messenger.camera;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import java.util.ArrayList;

public class CameraInfo
{
  protected Camera camera;
  protected int cameraId;
  protected final int frontCamera;
  protected ArrayList<Size> pictureSizes = new ArrayList();
  protected ArrayList<Size> previewSizes = new ArrayList();
  
  public CameraInfo(int paramInt, Camera.CameraInfo paramCameraInfo)
  {
    this.cameraId = paramInt;
    this.frontCamera = paramCameraInfo.facing;
  }
  
  private Camera getCamera()
  {
    return this.camera;
  }
  
  public int getCameraId()
  {
    return this.cameraId;
  }
  
  public ArrayList<Size> getPictureSizes()
  {
    return this.pictureSizes;
  }
  
  public ArrayList<Size> getPreviewSizes()
  {
    return this.previewSizes;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/camera/CameraInfo.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */