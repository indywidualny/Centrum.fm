package org.indywidualni.centrumfm.util;

import android.util.Log;

import org.indywidualni.centrumfm.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@SuppressWarnings("UnusedDeclaration")
public abstract class Serializer {

    /**
     * Serialize serializable object to a file
     *
     * @param source   the given object
     * @param filename filename (without .ser)
     * @param <T>      type of the given object
     */
    public static <T> void serialize(T source, String filename) {
        FileOutputStream fos;
        ObjectOutputStream oos = null;

        // serialization
        try {
            fos = new FileOutputStream(MyApplication.getContextOfApplication()
                    .getFilesDir().getPath() + File.separator + filename + ".ser");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(source);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (oos != null)
                    oos.close();
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        }
    }

    /**
     * Deserialize serialized object
     *
     * @param target   an object for a result
     * @param filename filename (without .ser)
     * @param <T>      type of a result
     * @return deserialized object (saved into target)
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(T target, String filename) {
        FileInputStream fis;
        ObjectInputStream ois = null;

        try {
            // deserialization
            fis = new FileInputStream(MyApplication.getContextOfApplication()
                    .getFilesDir().getPath() + File.separator + filename + ".ser");
            ois = new ObjectInputStream(fis);
            // read object from a file
            target = (T) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            Log.e("Serializer", "Class not found");
            c.printStackTrace();
        } finally {
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        }

        return target;
    }

}
