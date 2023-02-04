package org.wallentines.mdcfg.codec;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FileCodecRegistry {

    private FileCodec defaultCodec;
    private final HashMap<String, FileCodec> codecs = new HashMap<>();

    public void registerFileCodec(FileCodec codec) {

        if(defaultCodec == null) {
            this.defaultCodec = codec;
        }

        for(String extension : codec.getSupportedExtensions()) {
            this.codecs.put(extension, codec);
        }
    }

    public FileCodec forFile(File file) {

        String name = file.getName();
        int index = name.lastIndexOf('.');
        if(index == -1) return null;

        String ext = name.substring(index + 1);
        return codecs.get(ext);
    }

    public FileCodec forFileExtension(String ext) {
        return codecs.get(ext);
    }

    public <T> FileWrapper<T> fromFile(SerializeContext<T> context, File file) {
        return fromFile(context, file, StandardCharsets.UTF_8);
    }

    public <T> FileWrapper<T> fromFile(SerializeContext<T> context, File file, Charset charset) {

        FileCodec codec = forFile(file);
        if(codec == null) return null;

        FileWrapper<T> wrapper = new FileWrapper<>(context, codec, file, charset);
        wrapper.load();

        return wrapper;
    }

    public <T> FileWrapper<T> findOrCreate(SerializeContext<T> context, String prefix, File folder) {
        return findOrCreate(context, prefix, folder, StandardCharsets.UTF_8);
    }

    public <T> FileWrapper<T> findOrCreate(SerializeContext<T> context, String prefix, File folder, Charset charset) {

        if(!folder.isDirectory()) return null;

        File[] fs = folder.listFiles();
        if(fs != null) for(File file : fs) {

            String name = file.getName();
            int index = name.lastIndexOf('.');
            if(index == -1) return null;

            String fileName = name.substring(0, index);
            String extension = name.substring(index + 1);

            FileCodec codec = forFileExtension(extension);

            if(fileName.equals(prefix) && codec != null) {
                FileWrapper<T> out = new FileWrapper<>(context, codec, file, charset);
                out.load();
                return out;
            }
        }

        FileCodec codec = defaultCodec;

        String newFileName = prefix + "." + codec.getDefaultExtension();
        File newFile = new File(folder, newFileName);

        return new FileWrapper<>(context, codec, newFile, charset);
    }

}