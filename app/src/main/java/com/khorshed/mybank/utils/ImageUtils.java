package com.khorshed.mybank.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Utility class for image operations including Base64 conversion
 * Used for storing images in Firebase Database instead of Firebase Storage
 */
public class ImageUtils {

    /**
     * Convert image URI to Base64 string
     * 
     * @param context Application context
     * @param imageUri URI of the image from gallery
     * @param quality Compression quality (0-100), recommended 75 for balance
     * @return Base64 encoded string or null if conversion fails
     */
    public static String convertUriToBase64(Context context, Uri imageUri, int quality) {
        try {
            // Step 1: Open input stream from URI
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }
            
            // Step 2: Decode stream to Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (bitmap == null) {
                return null;
            }
            
            // Step 3: Convert Bitmap to Base64
            return convertBitmapToBase64(bitmap, quality);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert Bitmap to Base64 string
     * 
     * @param bitmap The bitmap to convert
     * @param quality Compression quality (0-100)
     * @return Base64 encoded string or null if conversion fails
     */
    public static String convertBitmapToBase64(Bitmap bitmap, int quality) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Compress bitmap to JPEG format
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            
            // Encode byte array to Base64 string
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert Base64 string to Bitmap
     * 
     * @param base64String Base64 encoded image string
     * @return Decoded Bitmap or null if decoding fails
     */
    public static Bitmap convertBase64ToBitmap(String base64String) {
        try {
            if (base64String == null || base64String.isEmpty()) {
                return null;
            }
            
            // Decode Base64 string to byte array
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            
            // Convert byte array to Bitmap
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resize bitmap to reduce size before converting to Base64
     * Useful to keep database storage size reasonable
     * 
     * @param bitmap Original bitmap
     * @param maxWidth Maximum width in pixels
     * @param maxHeight Maximum height in pixels
     * @return Resized bitmap
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Calculate scaling ratio
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }

            // Create scaled bitmap
            return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
            
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    /**
     * Get the size of Base64 string in KB
     * 
     * @param base64String Base64 encoded string
     * @return Size in KB
     */
    public static float getBase64SizeInKB(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return 0;
        }
        return base64String.length() / 1024f;
    }

    /**
     * Get the size of Base64 string in MB
     * 
     * @param base64String Base64 encoded string
     * @return Size in MB
     */
    public static float getBase64SizeInMB(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return 0;
        }
        return base64String.length() / (1024f * 1024f);
    }

    /**
     * Compress bitmap to fit within size limit
     * Automatically adjusts quality to meet size requirement
     * 
     * @param bitmap Bitmap to compress
     * @param maxSizeKB Maximum size in KB
     * @return Base64 string within size limit or null if cannot compress enough
     */
    public static String compressBitmapToBase64WithSizeLimit(Bitmap bitmap, int maxSizeKB) {
        try {
            int quality = 90; // Start with 90% quality
            String base64String;
            
            do {
                base64String = convertBitmapToBase64(bitmap, quality);
                
                if (base64String == null) {
                    return null;
                }
                
                float sizeKB = getBase64SizeInKB(base64String);
                
                if (sizeKB <= maxSizeKB) {
                    return base64String;
                }
                
                quality -= 5; // Reduce quality by 5%
                
            } while (quality >= 20); // Don't go below 20% quality
            
            // If still too large, return null
            return null;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validate if image size is acceptable for database storage
     * 
     * @param base64String Base64 encoded image
     * @param maxSizeKB Maximum allowed size in KB
     * @return true if size is acceptable, false otherwise
     */
    public static boolean isImageSizeAcceptable(String base64String, int maxSizeKB) {
        float sizeKB = getBase64SizeInKB(base64String);
        return sizeKB <= maxSizeKB;
    }
}
