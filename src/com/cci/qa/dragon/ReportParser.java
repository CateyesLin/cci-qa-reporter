package com.cci.qa.dragon;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ReportParser {
    public final static String
        DATA_SOURCE = "data",	//資料來源資料夾名稱
        
        DATA_CSV = "data.csv",
        
        CATEGORY_INDOOR = "Indoor",
        CATEGORY_OUTDOOR = "Outdoor",
    
        DIR_IMAGE = "data",
        DIR_IMAGE_ORIG = "orig",
        DIR_IMAGE_THUMB = "thumb";
    
    private final static int
    	TARGET_WIDTH = 300,
    	IMAGE_RATIO = 16;
    
    public final static String[] ACCEPT_EXTENSIONS = new String[] {".jpeg", ".jpg", ".png", ".bmp", ".gif"};
    private final static FilenameFilter IMAGE_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            String filenameLower = name.toLowerCase();
            for(String extension : ACCEPT_EXTENSIONS) {
                if(filenameLower.endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    };

    public static void main(String[] args) {
        List<File> deviceDirectories = new ArrayList<File>();
        File[] files = new File(DATA_SOURCE).listFiles();
        if(null != files) {
            for(File f : files) {
                if(f.isDirectory()) {
                    deviceDirectories.add(f);
                }
            }
        } else {
            System.out.print("[Error] Please create folder \"data\" and put device folder in it.");
            return;
        }
        
        if(0 == deviceDirectories.size()) {
            System.out.print("[Error] There is no devices folder in data.");
            return;
        }
        
        List<File>
            indoorDeviceFiles = new ArrayList<File>(),
            outdoorDeviceFiles = new ArrayList<File>();
        for(File dir : deviceDirectories) {
            File[] categories = dir.listFiles();
            
            if(null == categories) {
                System.out.print("[Warning] There is no Indoor and Outdoor folder in " + dir.getName() + ".\n");
                continue;
            }
            for(File c : categories) {
                if(c.isDirectory()) {
                    if(c.getName().equals(CATEGORY_INDOOR)) {
                        indoorDeviceFiles.add(c);
                    }
                    if(c.getName().equals(CATEGORY_OUTDOOR)) {
                        outdoorDeviceFiles.add(c);
                    }
                }
            }
        }
        
        if(0 != indoorDeviceFiles.size() || 0 != outdoorDeviceFiles.size()) {
            SimpleDateFormat formator = new SimpleDateFormat("yyyyMMddHHmmss");
            String now = formator.format(new Date());
            File reportFolder = new File(now);
            reportFolder.mkdir();
            getImagesFolder(reportFolder);
            copyOriginalImage(reportFolder);
            
            if(0 == indoorDeviceFiles.size()) {
                System.out.print("[Info] No indoor report will generate.\n");
            } else {
//            	mkdirs(reportFolder, CATEGORY_INDOOR);
                generateIndoorReport(reportFolder, indoorDeviceFiles);
            }
            if(0 == outdoorDeviceFiles.size()) {
                System.out.print("[Info] No outdoor report will generate.\n");
            } else {
//            	mkdirs(reportFolder, CATEGORY_OUTDOOR);
                generateOutdoorReport(reportFolder, outdoorDeviceFiles);
            }
        } else {
            System.out.print("[Error] No Indoor and Outdoor");
        }
    }
    
//    private static void mkdirs(File root, String category) {
//    	getOriginalImageFolder(root, category);
//    	getThumbImageFolder(root, category);
//    }
    
    private static File getImagesFolder(File root) {
    	File images = new File(root + File.separator + DIR_IMAGE);
    	images.mkdirs();
    	return images;
    }
    
    private static File getThumbFolder(File root) {
    	File images = new File(root + File.separator + DIR_IMAGE_THUMB);
    	images.mkdirs();
    	return images;
    }
    
    private static File getCSVFile(String category) {
    	File csv = new File(DATA_SOURCE + File.separator + category + ".csv");
		return csv;
    }
    
//    private static File getOriginalImageFolder(File root, String category) {
//    	File images = getImagesFolder(root);
//    	File original = new File(images.getPath() + File.separator + category + File.separator + DIR_IMAGE_ORIG);
//    	original.mkdirs();
//    	return original;
//    }
//    
//    private static File getThumbImageFolder(File root, String category) {
//    	File images = getImagesFolder(root);
//    	File thumb = new File(images.getPath() + File.separator + category + File.separator + DIR_IMAGE_ORIG);
//    	thumb.mkdirs();
//    	return thumb;
//    }
    
    private static void generateIndoorReport(File root, List<File> devices) {
        HashMap<String, Object> dataModel = new HashMap<String, Object>();
        
        List<Scene> scenes = getSceneData(devices, CATEGORY_INDOOR);
        generateThumb(root, scenes, CATEGORY_INDOOR);
        dataModel.put("scenes", scenes);
        dataModel.put("devices", getDevicesName(devices));
        
        Configuration cfg = new Configuration();
        try {
            Template template = cfg.getTemplate("template/indoor.ftl");
            Writer file = new FileWriter(new File(root.getPath() + File.separator + "indoor.html"));
            template.process(dataModel, file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        
    }
    
    private static void generateOutdoorReport(File root, List<File> devices) {
    	HashMap<String, Object> dataModel = new HashMap<String, Object>();
        
        List<Scene> scenes = getSceneData(devices, CATEGORY_OUTDOOR);
        generateThumb(root, scenes, CATEGORY_OUTDOOR);
        dataModel.put("scenes", scenes);
        dataModel.put("devices", getDevicesName(devices));
        
        Configuration cfg = new Configuration();
        try {
            Template template = cfg.getTemplate("template/outdoor.ftl");
            Writer file = new FileWriter(new File(root.getPath() + File.separator + "outdoor.html"));
            template.process(dataModel, file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }
    
    private static void generateThumb(File root, List<Scene> scenes, String category) {
    	File thumbFolder = getThumbFolder(root);
    	for(Scene scene : scenes) {
    		Map<String, List<File>> scenePhotos = scene.getDevicesPhoto();
    		for(String device : scenePhotos.keySet()) {
    			List<File> photos = scenePhotos.get(device);
    			for(File photo : photos) {
    				BufferedImage orig;
					try {
						orig = ImageIO.read(photo);
						BufferedImage thumb = Scalr.resize(orig, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, TARGET_WIDTH, Scalr.OP_ANTIALIAS);
						
						File folder = new File(thumbFolder.getPath() + File.separator + device + File.separator + category + File.separator + scene.getName());
						folder.mkdirs();
						
						File thumbFile = new File(folder.getPath() + File.separator + photo.getName());
						ImageIO.write(thumb, "jpg", thumbFile);
					} catch (IOException e) {
						System.out.print(String.format("[Error] Cannot generate thumb from:%s.\n", photo.getPath()));
						e.printStackTrace();
					}
    				
    			}
    		}
    	}
    	
    }
    
    private static void copyOriginalImage(File root) {
    	File destination = getImagesFolder(root);
    			
    	File source = new File(DATA_SOURCE);
    	try {
			FileUtils.copyDirectory(source, destination);
		} catch (IOException e) {
			System.out.print(String.format("[Error] Cannot copy dir: %s to %s.\n", source.getPath(), destination.getPath()));
			e.printStackTrace();
		}
    }
    
    private static List<Scene> getSceneData(List<File> devicesCategory, String category) {
    	HashMap<String, SceneCSVData> csvData = new HashMap<String, SceneCSVData>();
    	Reader in;
		try {
			in = new FileReader(getCSVFile(category));
			Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader("Motive", "Conditions", "Assessment Criteria").parse(in);
	    	for (CSVRecord record : records) {
	    	  String motive = record.get("Motive");
	    	  String condition = record.get("Conditions");
	    	  String criteria = record.get("Assessment Criteria");
	    	  csvData.put(motive, new SceneCSVData(condition, criteria));
	    	}
	    	in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	
        List<Scene> scenes = new ArrayList<Scene>();
        TreeSet<String> sceneNameList = getScenes(devicesCategory);
        
        for(String name : sceneNameList) {
        	SceneCSVData meta = csvData.get(name);
        	String
        		condition = "",
        		criteria = "";
        	if(null != meta) {
        		condition = meta.condition;
        		criteria = meta.criteria;
        	} else {
        		System.out.print(String.format("[Warning] Cannot find meta in csv for scene: %s.\n", name));
        	}
            Scene scene = new Scene(name, condition, criteria);
            for(File device : devicesCategory) {
            	String deviceName = device.getParentFile().getName();
                scene.create(deviceName);
                
                File sceneFolder = new File(device.getPath() + File.separator + name);
                if(sceneFolder.exists() && sceneFolder.isDirectory()) {
                    File[] photos = sceneFolder.listFiles(IMAGE_FILTER);
                    if(null != photos) {
                        for(File p : photos) {
                            scene.addPhoto(deviceName, p);
                        }
                    }
                }
            }
            scenes.add(scene);
        }
        return scenes;
    }
    
    private static List<String> getDevicesName(List<File> devices) {
        List<String> devicesName = new ArrayList<String>();
        for(File f : devices) {
            devicesName.add(f.getParentFile().getName());
        }
        return devicesName;
    }
    
    private static TreeSet<String> getScenes(List<File> devicesCategory) {
        TreeSet<String> scenes = new TreeSet<String>();
        
        for(File device : devicesCategory) {
            File[] sceneFolders = device.listFiles();
            if(null != sceneFolders) {
                for(File f : sceneFolders) {
                    if(f.isDirectory()) {
                        scenes.add(f.getName());
                    }
                }
            }
        }
        
        return scenes;
    }
}