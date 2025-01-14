package team.creative.cmdcam.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;
import team.creative.cmdcam.CMDCam;
import team.creative.cmdcam.common.util.CamPath;

public class CamSaveData extends WorldSavedData {
    
    public static final String DATA_NAME = CMDCam.MODID + "_Paths";
    
    private HashMap<String, CamPath> paths = new HashMap<>();
    
    public CamSaveData() {
        super(DATA_NAME);
    }
    
    public CamSaveData(String name) {
        super(name);
    }
    
    public CamPath get(String key) {
        return paths.get(key);
    }
    
    public void set(String key, CamPath path) {
        paths.put(key, path);
        setDirty();
    }
    
    public boolean remove(String key) {
        return paths.remove(key) != null;
    }
    
    public Collection<String> names() {
        return paths.keySet();
    }
    
    public void clear() {
        paths.clear();
        setDirty();
    }
    
    @Override
    public void load(CompoundNBT nbt) {
        for (String key : nbt.getAllKeys())
            paths.put(key, new CamPath(nbt.getCompound(key)));
    }
    
    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        for (Entry<String, CamPath> entry : paths.entrySet())
            nbt.put(entry.getKey(), entry.getValue().writeToNBT(new CompoundNBT()));
        return nbt;
    }
    
}
