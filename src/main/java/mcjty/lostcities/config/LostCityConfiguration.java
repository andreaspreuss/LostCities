package mcjty.lostcities.config;

import mcjty.lostcities.LostCities;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.*;

public class LostCityConfiguration {

    public static final String CATEGORY_GENERAL = "general";

    public static final String ASSET_COMMENT = "List of asset libraries loaded in the specified order. " +
            "If the path starts with '/' it is going to be loaded directly from the classpath. If the path starts with '$' it is loaded from the config directory";
    public static final String WORLDTYPES_COMMENT = "List of other worldtypes (id) that this mod will try " +
            "to work with. The worldtype has to support the IChunkPrimerFactory API for this to work";
    public static final String PROFILES_COMMENT = "List of all supported profiles (used for world creation). Warning! Make sure there is always a 'default' profile!";

    public static String[] ASSETS = new String[] {
            "/assets/lostcities/citydata/palette.json",
            "/assets/lostcities/citydata/palette_desert.json",
            "/assets/lostcities/citydata/palette_chisel.json",
            "/assets/lostcities/citydata/palette_chisel_desert.json",
            "/assets/lostcities/citydata/highwayparts.json",
            "/assets/lostcities/citydata/railparts.json",
            "/assets/lostcities/citydata/library.json",
            "$lostcities/userassets.json"
    };

    public static String[] ADAPTING_WORLDTYPES = new String[] {};

    public static int VERSION = 1;

    public static final Map<String, LostCityProfile> profiles = new HashMap<>();
    public static final Map<String, LostCityProfile> standardProfiles = new HashMap<>();

    public static String DIMENSION_PROFILE = "default";
    public static int DIMENSION_ID = 111;
    public static boolean DIMENSION_BOP = true;

    public static String[] init(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General settings");

        int oldVersion = 0;
        if (cfg.hasKey(CATEGORY_GENERAL, "version")) {
            oldVersion = cfg.getInt("version", CATEGORY_GENERAL, VERSION, 0, 10000, "Config version. Do not modify this manually!");
        }

        Property versionProperty = new Property("version", Integer.toString(VERSION), Property.Type.INTEGER);
        versionProperty.setComment("Config version. Do not modify this manually!");
        cfg.getCategory(CATEGORY_GENERAL).put("version", versionProperty);

        initStandardProfiles();

        String[] profileList;

        if (oldVersion != VERSION) {
            LostCities.logger.info("Upgrading Lost Cities config from " + oldVersion + " to " + VERSION + "!");
            String[] configuredAssets = cfg.getStringList("assets", CATEGORY_GENERAL, ASSETS, ASSET_COMMENT);
            List<String> mergedAssets = new ArrayList<>();
            Collections.addAll(mergedAssets, ASSETS);
            for (String asset : configuredAssets) {
                if (!mergedAssets.contains(asset)) {
                    mergedAssets.add(asset);
                }
            }
            cfg.getCategory(CATEGORY_GENERAL).remove("assets");
            ASSETS = cfg.getStringList("assets", CATEGORY_GENERAL, mergedAssets.toArray(new String[mergedAssets.size()]), ASSET_COMMENT);


            String[] defaultValues = {"default", "nodamage", "rarecities", "onlycities", "tallbuildings", "safe", "ancient", "wasteland", "chisel"};
            profileList = cfg.getStringList("profiles", CATEGORY_GENERAL,
                    defaultValues, PROFILES_COMMENT);
            List<String> mergedProfiles = new ArrayList<>();
            Collections.addAll(mergedProfiles, defaultValues);
            for (String profile : profileList) {
                if (!mergedProfiles.contains(profile)) {
                    mergedProfiles.add(profile);
                }
            }
            cfg.getCategory(CATEGORY_GENERAL).remove("profiles");
            profileList = cfg.getStringList("profiles", CATEGORY_GENERAL,
                    mergedProfiles.toArray(new String[mergedProfiles.size()]), PROFILES_COMMENT);
        } else {
            ASSETS = cfg.getStringList("assets", CATEGORY_GENERAL, ASSETS, ASSET_COMMENT);

            profileList = cfg.getStringList("profiles", CATEGORY_GENERAL,
                    new String[]{"default", "nodamage", "rarecities", "onlycities", "tallbuildings", "safe", "ancient", "chisel"}, PROFILES_COMMENT);

        }

        ADAPTING_WORLDTYPES = cfg.getStringList("adaptingWorldTypes", CATEGORY_GENERAL, ADAPTING_WORLDTYPES, WORLDTYPES_COMMENT);

        DIMENSION_PROFILE = cfg.getString("dimensionProfile", CATEGORY_GENERAL, DIMENSION_PROFILE, "The 'profile' to use for generation of the Lost City dimension");
        DIMENSION_ID = cfg.getInt("dimensionId", CATEGORY_GENERAL, DIMENSION_ID, -10000, 10000, "The 'ID' of the Lost City Dimension. Set to -1 if you don't want this dimension");
        DIMENSION_BOP = cfg.getBoolean("dimensionBoP", CATEGORY_GENERAL, DIMENSION_BOP, "If true and if Biomes O Plenty is present the dimension will use BoP biomes");

        return profileList;
    }

    private static void initStandardProfiles() {
        LostCityProfile profile = new LostCityProfile("default");
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("nodamage");
        profile.setDescription("Like default but no explosion damage");
        profile.EXPLOSION_CHANCE = 0;
        profile.MINI_EXPLOSION_CHANCE = 0;
        profile.PREVENT_LAKES_RAVINES_IN_CITIES = true;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("rarecities");
        profile.setDescription("Cities are rare");
        profile.CITY_CHANCE = 0.002f;
        profile.HIGHWAY_REQUIRES_TWO_CITIES = false;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("onlycities");
        profile.setDescription("The entire world is a city");
        profile.CITY_CHANCE = 0.2f;
        profile.CITY_MAXRADIUS = 256;
        profile.CITY_BIOME_FACTORS = new String[] { "river=.5", "frozen_river=.5", "ocean=.7", "frozen_ocean=.7", "deep_ocean=.6" };
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("tallbuildings");
        profile.setDescription("Very tall buildings (performance heavy)");
        profile.BUILDING_MINFLOORS = 4;
        profile.BUILDING_MINFLOORS_CHANCE = 8;
        profile.BUILDING_MAXFLOORS_CHANCE = 15;
        profile.BUILDING_MAXFLOORS = 20;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("safe");
        profile.setDescription("Safe mode: no spawners, lighting but no loot");
        profile.GENERATE_SPAWNERS = false;
        profile.GENERATE_LIGHTING = true;
        profile.GENERATE_LOOT = false;
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("ancient");
        profile.setDescription("Ancient jungle city, vines and leafs, small damage");
        profile.THICKNESS_OF_RANDOM_LEAFBLOCKS = 6;
        profile.CHANCE_OF_RANDOM_LEAFBLOCKS = .1f;
        profile.VINE_CHANCE = 0.09f;
        profile.EXPLOSION_CHANCE = 0;
        profile.MINI_EXPLOSION_CHANCE = .44f;
        profile.MINI_EXPLOSION_MAXRADIUS = 10;
        profile.ALLOWED_BIOME_FACTORS = new String[] { "jungle=1", "jungle_hills=1", "jungle_edge=1", "ocean=8", "beach=20", "river=5" };
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("wasteland");
        profile.setDescription("Wasteland, no water, bare land (can use BOP biomes)");
        profile.WATERLEVEL_OFFSET = 70;
        profile.GENERATE_LAKES = false;
        profile.GENERATE_OCEANMONUMENTS = false;
        profile.VINE_CHANCE = 0.003f;
        profile.CHANCE_OF_RANDOM_LEAFBLOCKS = 0.01f;
        profile.ALLOWED_BIOME_FACTORS = new String[] { "desert=1", "desert_hills=1", "stone_beach=1", "dead_forest=1", "gravel_beach=1", "outback=1", "volcanic_island=1", "wasteland=.3" };
        standardProfiles.put(profile.getName(), profile);

        profile = new LostCityProfile("chisel");
        profile.setDescription("Use Chisel blocks (only if chisel is available!)");
        profile.setWorldStyle("chisel");
        standardProfiles.put(profile.getName(), profile);


    }

}
