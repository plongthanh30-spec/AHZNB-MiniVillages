package com.example.ahznbminivillages;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.EntityEntry;

import java.io.File;
import java.util.Random;

@Mod(modid = AHZNBMiniVillages.MODID, name = "AHZNB Mini Villages", version = "1.0.0",
     dependencies = "after:ahznb")
public class AHZNBMiniVillages {
    public static final String MODID = "ahznbminivillages";

    public static Item SUMMONING_ORB;

    @Mod.EventBusSubscriber(modid = MODID)
    public static class ItemRegistry {
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            SUMMONING_ORB = new SummoningOrbItem();
            SUMMONING_ORB.setRegistryName(MODID, "bijuu_summoning_orb");
            SUMMONING_ORB.setUnlocalizedName(MODID + ".bijuu_summoning_orb");
            SUMMONING_ORB.setMaxStackSize(16);
            event.getRegistry().register(SUMMONING_ORB);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, "bijuu_summoning_orb"),
                new ResourceLocation(MODID, "recipes"),
                new ItemStack(SUMMONING_ORB),
                "EEE", "EDE", "EEE", 'E', Items.EMERALD, 'D', Items.DIAMOND);
    }

    static int spacingBlocks = 900;
    static String[] ahznbLoot = new String[] {
        "narutomod:anbu_sword",
        "narutomod:chakra_blades",
        "narutomod:chokuto",
        "narutomod:claw",
        "narutomod:cleaver",
        "narutomod:explosive_tag",
        "narutomod:folding_fan",
        "narutomod:team_scroll",
        "narutomod:steam_armorlegs",
        "narutomod:steam_armorhelmet",
        "narutomod:steam_armorbody",
        "narutomod:spear_retractable",
        "narutomod:sound_gaunlet",
        "narutomod:smoke_bomb",
        "narutomod:senbon",
        "narutomod:gaunlet",
        "narutomod:ichiraku_ramen",
        "narutomod:kunai_3prong",
        "narutomod:kunai_blade",
        "narutomod:kunai_explosive",
        "narutomod:military_rations_pill",
        "narutomod:military_rations_pill_gold",
        "narutomod:uchihaboots",
        "narutomod:ninja_armor_fishnetslegs",
        "narutomod:ninja_armor_amehelmet",
        "narutomod:ninja_armor_iwahelmet",
        "narutomod:ninja_armor_kirihelmet",
        "narutomod:ninja_armor_konohahelmet",
        "narutomod:ninja_armor_war_1helmet",
        "narutomod:ninja_armor_kumohelmet",
        "narutomod:ninja_armor_sunahelmet",
        "narutomod:ninja_armor_jumpsuitlegs",
        "narutomod:ninja_armor_anbulegs",
        "narutomod:ninja_armor_iwalegs",
        "narutomod:ninja_armor_kirilegs",
        "narutomod:ninja_armor_konohalegs",
        "narutomod:ninja_armor_kumolegs",
        "narutomod:ninja_armor_sunalegs",
        "narutomod:ninja_armor_anbubody",
        "narutomod:ninja_armor_iwabody",
        "narutomod:ninja_armor_kiribody",
        "narutomod:ninja_armor_konohabody",
        "narutomod:ninja_armor_kumobody",
        "narutomod:ninja_armor_sunabody",
        "narutomod:poisonbomb",
        "narutomod:poison_senbon",
        "narutomod:kunai",
        "narutomod:medical_scroll",
        "narutomod:shuriken",
        "narutomod:uchihalegs",
        "narutomod:uchihabody",
        "narutomod:samurai_armorbody",
        "narutomod:samurai_armorhelmet",
        "narutomod:samurai_armorlegs",
        "narutomod:senbon_arm"
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration cfg = new Configuration(new File(event.getModConfigurationDirectory(), MODID + ".cfg"));
        try {
            cfg.load();
            spacingBlocks = cfg.getInt("spacingBlocks", "worldgen", 900, 900, 900,
                    "Minimum center-to-center spacing between generated villages: 900 blocks.");
            ahznbLoot = cfg.getStringList("ahznbLoot", "loot", ahznbLoot,
                    "Exact AHZNB item registry IDs. Remove entries here to disable them from village chest loot.");
        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
        GameRegistry.registerWorldGenerator(new MiniVillageGenerator(), 10);
    }

    private static class MiniVillageLocations extends WorldSavedData {
        private static final String DATA_NAME = "ahznbminivillages_locations";
        private final java.util.List<int[]> centers = new java.util.ArrayList<int[]>();

        public MiniVillageLocations() { super(DATA_NAME); }
        public MiniVillageLocations(String name) { super(name); }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            centers.clear();
            NBTTagList list = nbt.getTagList("Centers", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                centers.add(new int[] {tag.getInteger("X"), tag.getInteger("Z")});
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            NBTTagList list = new NBTTagList();
            for (int[] center : centers) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("X", center[0]);
                tag.setInteger("Z", center[1]);
                list.appendTag(tag);
            }
            nbt.setTag("Centers", list);
            return nbt;
        }

        boolean isTooClose(int x, int z, int minDistance) {
            long minSq = (long) minDistance * minDistance;
            for (int[] center : centers) {
                long dx = (long)x - center[0];
                long dz = (long)z - center[1];
                if (dx * dx + dz * dz < minSq) return true;
            }
            return false;
        }

        void addCenter(int x, int z) {
            centers.add(new int[] {x, z});
            markDirty();
        }
    }

    private static MiniVillageLocations getVillageLocations(World world) {
        MapStorage storage = world.getPerWorldStorage();
        MiniVillageLocations data = (MiniVillageLocations) storage.getOrLoadData(
                MiniVillageLocations.class, MiniVillageLocations.DATA_NAME);
        if (data == null) {
            data = new MiniVillageLocations();
            storage.setData(MiniVillageLocations.DATA_NAME, data);
        }
        return data;
    }

    private static class MiniVillageGenerator implements IWorldGenerator {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world,
                             IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
            if (world.isRemote || world.provider.getDimension() != 0) return;

            // 2.5x the previous attempt rate (256 -> 640).
            double chancePerChunk = Math.min(1.0, 1200.0 / (spacingBlocks * (double) spacingBlocks));
            if (random.nextDouble() >= chancePerChunk) return;

            int x = chunkX * 16 + 3 + random.nextInt(10);
            int z = chunkZ * 16 + 3 + random.nextInt(10);
            BlockPos surface = world.getHeight(new BlockPos(x, 0, z));
            int y = surface.getY();
            // Allow lower terrain while avoiding bedrock-level placement.
            if (y < 2 || y > 250) return;
            if (!isMostlyClear(world, new BlockPos(x, y, z))) return;
            // Keep generated settlements well away from existing vanilla villages.
            if (world.getVillageCollection().getNearestVillage(new BlockPos(x, y, z), 160) != null) return;
            // Enforce real minimum spacing between this mod's villages.
            MiniVillageLocations locations = getVillageLocations(world);
            if (locations.isTooClose(x, z, spacingBlocks)) return;

            int villageType = Math.floorMod((chunkX * 73428767) ^ (chunkZ * 912931), 5);
            buildVillage(world, new BlockPos(x, y, z), villageType, random);
            locations.addCenter(x, z);
        }

        private boolean isMostlyClear(World world, BlockPos p) {
            // Reject tree trunks/canopies at the village center and across a wide sample
            // of the settlement footprint, so the village is placed on ground, not treetops.
            int[] samples = {-100, 0, 100};
            for (int dx : samples) {
                for (int dz : samples) {
                    BlockPos ground = world.getHeight(new BlockPos(p.getX() + dx, 0, p.getZ() + dz)).down();
                    Block block = world.getBlockState(ground).getBlock();
                    if (block instanceof net.minecraft.block.BlockLeaves ||
                        block instanceof net.minecraft.block.BlockLog ||
                        block == Blocks.BEDROCK || block == Blocks.WATER ||
                        block == Blocks.FLOWING_WATER || block == Blocks.LAVA ||
                        block == Blocks.FLOWING_LAVA) return false;
                }
            }
            // Also check the exact center column (getHeight can otherwise land on leaves).
            Block centerTop = world.getBlockState(p.down()).getBlock();
            return !(centerTop instanceof net.minecraft.block.BlockLeaves) &&
                   !(centerTop instanceof net.minecraft.block.BlockLog);
        }

        private void clearSurfaceArea(World world, BlockPos center) {
            // Clear trees, leaves, buildings and other obstructions above natural ground
            // in a 260-block radius. The terrain block itself is preserved.
            final int radius = 260;
            final int radiusSq = radius * radius;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radiusSq) continue;
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;
                    int y = world.getHeight(new BlockPos(x, 0, z)).getY() - 1;
                    y = Math.min(y, 254);
                    while (y > 1) {
                        Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                        if (block == Blocks.GRASS || block == Blocks.DIRT ||
                            block == Blocks.STONE || block == Blocks.SAND ||
                            block == Blocks.SANDSTONE || block == Blocks.GRAVEL ||
                            block == Blocks.CLAY || block == Blocks.MYCELIUM ||
                            block == Blocks.HARDENED_CLAY || block == Blocks.STAINED_HARDENED_CLAY ||
                            block == Blocks.SNOW || block == Blocks.SNOW_LAYER) {
                            break;
                        }
                        y--;
                    }
                    // Remove everything above the detected natural surface, not the terrain.
                    for (int clearY = y + 1; clearY <= 254; clearY++) {
                        BlockPos pos = new BlockPos(x, clearY, z);
                        if (!world.isAirBlock(pos)) {
                            world.setBlockToAir(pos);
                        }
                    }
                }
            }
        }

        private void buildVillage(World world, BlockPos p, int type, Random random) {
            IBlockState wall;
            IBlockState roof;
            IBlockState accent;
            switch (type) {
                case 0: // Konoha - wood and red roofs
                    wall = Blocks.PLANKS.getStateFromMeta(0);
                    roof = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(14);
                    accent = Blocks.LOG.getDefaultState();
                    break;
                case 1: // Suna - sandstone
                    wall = Blocks.SANDSTONE.getDefaultState();
                    roof = Blocks.SANDSTONE.getStateFromMeta(2);
                    accent = Blocks.SANDSTONE.getStateFromMeta(1);
                    break;
                case 2: // Iwa - stone
                    wall = Blocks.STONEBRICK.getDefaultState();
                    roof = Blocks.STONEBRICK.getStateFromMeta(3);
                    accent = Blocks.COBBLESTONE.getDefaultState();
                    break;
                case 3: // Kumo - bright quartz
                    wall = Blocks.QUARTZ_BLOCK.getDefaultState();
                    roof = Blocks.QUARTZ_BLOCK.getStateFromMeta(1);
                    accent = Blocks.IRON_BLOCK.getDefaultState();
                    break;
                default: // Kiri - dark brick
                    wall = Blocks.NETHER_BRICK.getDefaultState();
                    roof = Blocks.NETHER_BRICK.getDefaultState();
                    accent = Blocks.NETHER_BRICK.getDefaultState();
                    break;
            }

            // Eleven varied homes around a larger central residence.
            int[][] homes = {
                // Original eleven homes
                {-29,-27}, {-9,-34}, {14,-34}, {34,-22}, {37,2},
                {29,29}, {6,37}, {-19,32}, {-37,12}, {-35,-13}, {0,42},
                // Ten additional homes around the outer residential ring
                {-65,-45}, {-42,-68}, {-5,-72}, {38,-66}, {68,-42},
                {72,8}, {55,58}, {12,73}, {-38,66}, {-72,28},
            };
            for (int i=0; i<homes.length; i++) {
                BlockPos house = p.add(homes[i][0], 0, homes[i][1]);
                buildNinjaHouse(world, house, wall, roof, accent, random, type);
                // Small style variations: porch pillars, flower beds and side lamps.
                if (i%3==0) world.setBlockState(house.add(0,3,-6), accent, 2);
                if (i%3==1) world.setBlockState(house.add(0,3,6), Blocks.GLOWSTONE.getDefaultState(), 2);
                buildGardenTree(world, house.add(-9, 0, 6), random);
                buildGardenTree(world, house.add(9, 0, 6), random);
            }
            buildCentralResidence(world, p, wall, roof, accent, random);

            // Broad landscaped main avenue, branching paths and a decorated central square.
            for (int z=-38; z<=38; z++) for (int dx=-2; dx<=2; dx++)
                world.setBlockState(p.add(dx,-1,z), (dx==0 ? Blocks.STONEBRICK : Blocks.GRAVEL).getDefaultState(), 2);
            for (int x=-38; x<=38; x++) for (int dz=-2; dz<=2; dz++)
                world.setBlockState(p.add(x,-1,dz), (dz==0 ? Blocks.STONEBRICK : Blocks.GRAVEL).getDefaultState(), 2);
            for (int dx=-7; dx<=7; dx++) for (int dz=-7; dz<=7; dz++) {
                boolean edge=Math.abs(dx)==7 || Math.abs(dz)==7;
                world.setBlockState(p.add(dx,-1,dz), (edge ? Blocks.STONEBRICK.getDefaultState() : Blocks.STONE.getDefaultState()), 2);
            }
            buildVillageWalls(world, p, wall, accent);
            buildVillageGate(world, p.add(0,0,-105), accent, roof);
            buildVillageGarden(world, p, random);
            spawnBijuu(world, p, random);
            spawnVillageResidents(world, p, random);
        }

        private void buildNinjaHouse(World world, BlockPos p, IBlockState wall, IBlockState roof,
                                     IBlockState accent, Random random, int style) {
            // Japanese village-house design: stone plinth, timber frame, deep wraparound
            // eaves, steep layered roof, second-floor gable, balcony and detailed frontage.
            IBlockState timber = style==1 ? Blocks.PLANKS.getStateFromMeta(2) :
                                 style==2 ? Blocks.PLANKS.getStateFromMeta(1) :
                                 Blocks.PLANKS.getStateFromMeta(0);
            IBlockState darkWood = style==1 ? Blocks.LOG.getDefaultState() :
                                   style==2 ? Blocks.LOG.getStateFromMeta(1) :
                                   Blocks.LOG.getDefaultState();
            IBlockState glass = Blocks.STAINED_GLASS_PANE.getStateFromMeta(style==0 ? 0 : style==1 ? 4 : style==2 ? 7 : style==3 ? 11 : 15);
            IBlockState base = style==1 ? Blocks.SANDSTONE.getStateFromMeta(2) :
                               style==2 ? Blocks.STONEBRICK.getDefaultState() :
                               Blocks.STONEBRICK.getDefaultState();
            IBlockState fence = Blocks.OAK_FENCE.getDefaultState();
            IBlockState slab = style==1 ? Blocks.WOODEN_SLAB.getStateFromMeta(2) :
                               Blocks.WOODEN_SLAB.getDefaultState();
            int width = style==1 ? 7 : 6;
            int depth = style==2 ? 7 : 6;

            // Raised stone foundation and warm timber floor.
            for (int x=-width-1;x<=width+1;x++) for (int z=-depth-1;z<=depth+1;z++) {
                world.setBlockState(p.add(x,-1,z), base, 2);
                if (Math.abs(x)<=width && Math.abs(z)<=depth)
                    world.setBlockState(p.add(x,0,z), timber, 2);
            }

            // Ground floor shell with recessed wall panels and a continuous timber frame.
            for (int y=1;y<=4;y++) for (int x=-width;x<=width;x++) for (int z=-depth;z<=depth;z++) {
                if (Math.abs(x)!=width && Math.abs(z)!=depth) continue;
                world.setBlockState(p.add(x,y,z), wall, 2);
            }
            for (int x : new int[]{-width,width}) for (int z : new int[]{-depth,depth})
                for (int y=1;y<=7;y++) world.setBlockState(p.add(x,y,z), darkWood, 2);
            for (int x=-width;x<=width;x+=3) {
                for (int y=1;y<=4;y++) {
                    world.setBlockState(p.add(x,y,-depth), darkWood, 2);
                    world.setBlockState(p.add(x,y,depth), darkWood, 2);
                }
            }
            for (int z=-depth;z<=depth;z+=3) for (int y=1;y<=4;y++) {
                world.setBlockState(p.add(-width,y,z), darkWood, 2);
                world.setBlockState(p.add(width,y,z), darkWood, 2);
            }

            // Shoji-style window bays on all four sides.
            for (int y=2;y<=3;y++) {
                for (int x=-width+2;x<=width-2;x++) {
                    if (Math.abs(x)%3!=0) {
                        world.setBlockState(p.add(x,y,-depth), glass, 2);
                        world.setBlockState(p.add(x,y,depth), glass, 2);
                    }
                }
                for (int z=-depth+2;z<=depth-2;z++) {
                    if (Math.abs(z)%3!=0) {
                        world.setBlockState(p.add(-width,y,z), glass, 2);
                        world.setBlockState(p.add(width,y,z), glass, 2);
                    }
                }
            }

            // Front entrance, framed doorway, lanterns and broad stone approach.
            for (int y=1;y<=2;y++) world.setBlockToAir(p.add(0,y,depth));
            world.setBlockState(p.add(0,1,depth), Blocks.OAK_DOOR.getStateFromMeta(0), 2);
            world.setBlockState(p.add(0,2,depth), Blocks.OAK_DOOR.getStateFromMeta(8), 2);
            for (int x : new int[]{-2,2}) {
                world.setBlockState(p.add(x,1,depth), darkWood, 2);
                world.setBlockState(p.add(x,2,depth), Blocks.GLOWSTONE.getDefaultState(), 2);
                world.setBlockState(p.add(x,3,depth), darkWood, 2);
            }
            for (int step=1;step<=4;step++) for (int x=-2;x<=2;x++)
                world.setBlockState(p.add(x,0,depth+step), base, 2);
            // Veranda around the front and sides.
            for (int x=-width-1;x<=width+1;x++) {
                world.setBlockState(p.add(x,1,depth+1), slab, 2);
                world.setBlockState(p.add(x,1,-depth-1), slab, 2);
            }
            for (int z=-depth;z<=depth;z++) {
                world.setBlockState(p.add(-width-1,1,z), slab, 2);
                world.setBlockState(p.add(width+1,1,z), slab, 2);
            }
            for (int x=-width;x<=width;x+=3) {
                world.setBlockState(p.add(x,2,depth+1), fence, 2);
                world.setBlockState(p.add(x,2,-depth-1), fence, 2);
            }

            // Second storey is narrower, with a projecting balcony and a prominent gable.
            for (int x=-4;x<=4;x++) for (int z=-4;z<=4;z++) {
                if (Math.abs(x)==4 || Math.abs(z)==4)
                    world.setBlockState(p.add(x,5,z), timber, 2);
            }
            for (int y=6;y<=8;y++) {
                for (int x=-4;x<=4;x++) {
                    world.setBlockState(p.add(x,y,-4), wall, 2);
                    world.setBlockState(p.add(x,y,4), wall, 2);
                }
                for (int z=-3;z<=3;z++) {
                    world.setBlockState(p.add(-4,y,z), wall, 2);
                    world.setBlockState(p.add(4,y,z), wall, 2);
                }
            }
            for (int x : new int[]{-4,4}) for (int y=5;y<=9;y++) {
                world.setBlockState(p.add(x,y,-4), darkWood, 2);
                world.setBlockState(p.add(x,y,4), darkWood, 2);
            }
            for (int x=-2;x<=2;x++) for (int y=6;y<=7;y++) {
                world.setBlockState(p.add(x,y,4), glass, 2);
            }
            for (int x=-5;x<=5;x++) {
                world.setBlockState(p.add(x,5,5), slab, 2);
                if (x%2==0) world.setBlockState(p.add(x,6,5), fence, 2);
            }

            // Deep layered Japanese roof: wide lower eaves and progressively smaller
            // upper courses create a sloped silhouette rather than a flat box roof.
            for (int layer=0;layer<4;layer++) {
                int rx=width+3-layer;
                int rz=depth+3-layer;
                int y=5+layer;
                for (int x=-rx;x<=rx;x++) for (int z=-rz;z<=rz;z++) {
                    if (Math.abs(x)>=rx-1 || Math.abs(z)>=rz-1)
                        world.setBlockState(p.add(x,y,z), roof, 2);
                }
            }
            for (int layer=0;layer<3;layer++) {
                int rx=5-layer;
                int rz=5-layer;
                int y=9+layer;
                for (int x=-rx;x<=rx;x++) for (int z=-rz;z<=rz;z++) {
                    if (Math.abs(x)>=rx-1 || Math.abs(z)>=rz-1)
                        world.setBlockState(p.add(x,y,z), roof, 2);
                }
            }
            for (int x=-2;x<=2;x++) for (int z=-2;z<=2;z++)
                world.setBlockState(p.add(x,12,z), roof, 2);
            // Curled-up roof corners and ridge ornaments.
            for (int x : new int[]{-width-3,width+3}) for (int z : new int[]{-depth-3,depth+3}) {
                world.setBlockState(p.add(x,6,z), accent, 2);
                world.setBlockState(p.add(x,7,z), roof, 2);
            }
            world.setBlockState(p.add(0,13,0), Blocks.GLOWSTONE.getDefaultState(), 2);

            // Extra traditional detailing: layered fascia, exposed rafters, window
            // shutters, decorative gable frame and a covered entrance canopy.
            for (int x=-width-2;x<=width+2;x++) {
                world.setBlockState(p.add(x,5,depth+2), darkWood, 2);
                world.setBlockState(p.add(x,5,-depth-2), darkWood, 2);
            }
            for (int z=-depth-1;z<=depth+1;z+=2) {
                world.setBlockState(p.add(-width-2,5,z), darkWood, 2);
                world.setBlockState(p.add(width+2,5,z), darkWood, 2);
            }
            for (int x=-4;x<=4;x++) {
                world.setBlockState(p.add(x,8,5), darkWood, 2);
                if (Math.abs(x)<=2) world.setBlockState(p.add(x,9,5), glass, 2);
            }
            // Decorative triangular gable on the front elevation.
            for (int level=0;level<=3;level++) {
                int span=3-level;
                for (int x=-span;x<=span;x++)
                    world.setBlockState(p.add(x,8+level,4), level==3 ? roof : darkWood, 2);
            }
            // Entrance canopy with two timber brackets and lanterns.
            for (int x=-2;x<=2;x++) {
                world.setBlockState(p.add(x,4,depth+2), roof, 2);
                world.setBlockState(p.add(x,4,depth+3), roof, 2);
            }
            for (int x : new int[]{-2,2}) {
                world.setBlockState(p.add(x,3,depth+2), darkWood, 2);
                world.setBlockState(p.add(x,4,depth+2), Blocks.GLOWSTONE.getDefaultState(), 2);
            }
            // Low stone edging and flower planters at the front corners.
            for (int x : new int[]{-width-1,width+1}) {
                for (int z=depth+2;z<=depth+4;z++)
                    world.setBlockState(p.add(x,0,z), base, 2);
                world.setBlockState(p.add(x,1,depth+3), Blocks.YELLOW_FLOWER.getDefaultState(), 2);
            }

            // Interior furnishings and guaranteed Naruto loot chest.
            world.setBlockState(p.add(-3,1,-2), Blocks.BOOKSHELF.getDefaultState(), 2);
            world.setBlockState(p.add(-2,1,-2), Blocks.BOOKSHELF.getDefaultState(), 2);
            world.setBlockState(p.add(3,1,-2), Blocks.CRAFTING_TABLE.getDefaultState(), 2);
            world.setBlockState(p.add(3,1,-1), Blocks.FURNACE.getDefaultState(), 2);
            world.setBlockState(p.add(-3,1,2), Blocks.BED.getStateFromMeta(0), 2);
            world.setBlockState(p.add(-3,1,3), Blocks.BED.getStateFromMeta(8), 2);
            placeLootChest(world, p.add(2,1,-3), random);

            // A small garden edge makes each generated home feel inhabited.
            for (int x=-3;x<=3;x+=2) {
                world.setBlockState(p.add(x,0,depth+6), Blocks.YELLOW_FLOWER.getDefaultState(), 2);
                world.setBlockState(p.add(x,0,depth+7), Blocks.LEAVES.getDefaultState(), 2);
            }
        }

        private int spruceMeta() { return 1; }

        private void spawnFukasakuInCentralResidence(World world, BlockPos center) {
            // Dan's D Sage Training identifies shrine NPCs by their exact AHZNB inner class
            // and the ToadSageShrineNPC/home NBT tags.
            for (EntityEntry entry : ForgeRegistries.ENTITIES.getValuesCollection()) {
                Class<? extends Entity> entityClass = entry.getEntityClass();
                if (entityClass == null ||
                    !entityClass.getName().contains("EntityToadFukasaku$EntityCustom")) continue;

                Entity fukasaku = entry.newInstance(world);
                if (fukasaku == null) return;

                double homeX = center.getX() + 2.5;
                double homeY = center.getY() + 1.0;
                double homeZ = center.getZ() + 2.5;
                fukasaku.setPosition(homeX, homeY, homeZ);

                NBTTagCompound data = fukasaku.getEntityData();
                data.setBoolean("ToadSageShrineNPC", true);
                data.setDouble("ToadSageHomeX", homeX);
                data.setDouble("ToadSageHomeY", homeY);
                data.setDouble("ToadSageHomeZ", homeZ);
                data.setDouble("ToadSageHomeRadius", 2.25D);

                world.spawnEntity(fukasaku);
                return;
            }
        }

        private void buildCentralResidence(World world, BlockPos p, IBlockState wall, IBlockState roof, IBlockState accent, Random random) {
            spawnFukasakuInCentralResidence(world, p);
            for (int dx=-8; dx<=8; dx++) for (int dz=-8; dz<=8; dz++) {
                world.setBlockState(p.add(dx,-1,dz), accent, 2);
                world.setBlockState(p.add(dx,0,dz), Blocks.PLANKS.getDefaultState(), 2);
                if (Math.abs(dx)==8 || Math.abs(dz)==8) {
                    for (int y=1; y<=4; y++) world.setBlockState(p.add(dx,y,dz), wall, 2);
                    for (int y=6; y<=9; y++) world.setBlockState(p.add(dx,y,dz), wall, 2);
                }
            }
            for (int dx=-8; dx<=8; dx++) for (int dz=-8; dz<=8; dz++) world.setBlockState(p.add(dx,5,dz), Blocks.PLANKS.getDefaultState(), 2);
            for (int y=1; y<=3; y++) for (int dx=-1; dx<=1; dx++) world.setBlockToAir(p.add(dx,y,-8));
            for (int y=6; y<=8; y++) for (int dx=-1; dx<=1; dx++) world.setBlockToAir(p.add(dx,y,-8));
            for (int layer=0; layer<4; layer++) { int r=10-layer*2; for (int dx=-r; dx<=r; dx++) for (int dz=-r; dz<=r; dz++) world.setBlockState(p.add(dx,10+layer,dz), roof, 2); }
            world.setBlockState(p.add(0,3,0), Blocks.GLOWSTONE.getDefaultState(), 2);
            world.setBlockState(p.add(0,8,0), Blocks.GLOWSTONE.getDefaultState(), 2);
            world.setBlockState(p.add(-7,3,0), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(7,3,0), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(-7,8,0), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(7,8,0), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(-5,1,3), Blocks.BED.getStateFromMeta(0), 2);
            world.setBlockState(p.add(-5,1,4), Blocks.BED.getStateFromMeta(8), 2);
            world.setBlockState(p.add(-4,6,3), Blocks.BED.getStateFromMeta(0), 2);
            world.setBlockState(p.add(-4,6,4), Blocks.BED.getStateFromMeta(8), 2);
            world.setBlockState(p.add(-4,1,-3), Blocks.CRAFTING_TABLE.getDefaultState(), 2);
            world.setBlockState(p.add(-3,1,-3), Blocks.FURNACE.getDefaultState(), 2);
            // Four separate loot chests in the central residence.
            placeLootChest(world, p.add(-5,1,-5), random);
            placeLootChest(world, p.add(5,1,-5), random);
            placeLootChest(world, p.add(-5,1,5), random);
            placeLootChest(world, p.add(5,1,5), random);
        }

        private void buildVillageWalls(World world, BlockPos p, IBlockState wall, IBlockState accent) {
            // Rasterize the entire circular perimeter densely so adjacent wall blocks touch.
            // Leave only the centered northern gate opening; the gate towers close its edges.
            int radius = 105;
            int previousX = 0, previousZ = -radius;
            for (int step = 1; step <= 3600; step++) {
                double angle = Math.toRadians(step / 10.0);
                int x = (int)Math.round(Math.cos(angle) * radius);
                int z = (int)Math.round(Math.sin(angle) * radius);
                drawWallSegment(world, p, previousX, previousZ, x, z, wall, accent);
                previousX = x;
                previousZ = z;
            }
            // Re-open a clean 11-block-wide, two-block-deep gate passage.
            for (int x = -5; x <= 5; x++) for (int y = 0; y <= 6; y++) {
                world.setBlockToAir(p.add(x, y, -105));
                world.setBlockToAir(p.add(x, y, -104));
            }
        }

        private void drawWallSegment(World world, BlockPos p, int x1, int z1, int x2, int z2,
                                     IBlockState wall, IBlockState accent) {
            int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
            for (int i = 0; i <= steps; i++) {
                double t = steps == 0 ? 0.0 : i / (double)steps;
                int x = (int)Math.round(x1 + (x2 - x1) * t);
                int z = (int)Math.round(z1 + (z2 - z1) * t);
                // Two-block-thick continuous wall, seven blocks tall (Y=0..6).
                // The second course is offset inward to form a solid defensive wall.
                for (int y = 0; y <= 6; y++) {
                    IBlockState course = (y == 6) ? accent : wall;
                    world.setBlockState(p.add(x, y, z), course, 2);
                    world.setBlockState(p.add(x, y, z + (z < 0 ? 1 : -1)), course, 2);
                }
                // Stone merlons along the top give the wall a fortified silhouette.
                if ((x + z) % 5 == 0) {
                    world.setBlockState(p.add(x, 7, z), accent, 2);
                    world.setBlockState(p.add(x, 7, z + (z < 0 ? 1 : -1)), accent, 2);
                }
            }
        }

        private void spawnBijuu(World world, BlockPos center, Random random) {
            // 100% chance per generated village: always attempt one tailed-beast spawn.
            // Spawn outside the wall; never spawn the Ten Tails.
            java.util.List<EntityEntry> bijuuTypes = new java.util.ArrayList<EntityEntry>();
            for (EntityEntry entry : ForgeRegistries.ENTITIES.getValuesCollection()) {
                ResourceLocation id = entry.getRegistryName();
                if (id == null || !"narutomod".equals(id.getResourceDomain())) continue;
                String path = id.getResourcePath().toLowerCase();
                if ("one_tail".equals(path) || "two_tails".equals(path) ||
                    "three_tails".equals(path) || "four_tails".equals(path) ||
                    "five_tails".equals(path) || "six_tails".equals(path) ||
                    "seven_tails".equals(path) || "eight_tails".equals(path) ||
                    "nine_tails".equals(path)) {
                    bijuuTypes.add(entry);
                }
            }
            if (bijuuTypes.isEmpty()) return;
            Entity bijuu = bijuuTypes.get(random.nextInt(bijuuTypes.size())).newInstance(world);
            if (bijuu == null) return;
            // Wall radius is 105 blocks; place the beast 35 blocks beyond the wall.
            double angle = random.nextDouble() * Math.PI * 2.0;
            int distance = 140;
            BlockPos target = center.add((int)Math.round(Math.cos(angle) * distance), 0,
                                         (int)Math.round(Math.sin(angle) * distance));
            BlockPos spawn = world.getTopSolidOrLiquidBlock(target);
            bijuu.setPosition(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
            world.spawnEntity(bijuu);
        }

        private void buildDistinctVillageArchitecture(World world, BlockPos p, IBlockState wall, IBlockState roof, IBlockState accent, int type, Random random) {
            // Distinct facade details by hidden village type. The base house remains shared;
            // these additions give each settlement a recognizable silhouette.
            if (type == 0) { // Konoha: Japanese layered eaves and timber facade
                for (int z=-6; z<=6; z+=12) {
                    for (int x=-6; x<=6; x++) {
                        world.setBlockState(p.add(x,6,z), roof, 2);
                        world.setBlockState(p.add(x,7,z), wall, 2);
                    }
                }
                for (int x : new int[]{-5,5}) {
                    world.setBlockState(p.add(x,1,-6), Blocks.TORCH.getDefaultState(), 2);
                    world.setBlockState(p.add(x,5,-6), accent, 2);
                }
            } else if (type == 1) { // Suna: sandstone fortress parapets
                for (int x=-6; x<=6; x++) {
                    if (x%2==0) {
                        world.setBlockState(p.add(x,7,-6), accent, 2);
                        world.setBlockState(p.add(x,7,6), accent, 2);
                    }
                }
                for (int z=-5; z<=5; z+=5) for (int y=2; y<=6; y++)
                    world.setBlockState(p.add(-6,y,z), wall, 2);
            } else if (type == 2) { // Iwa: heavy stone buttresses
                for (int x : new int[]{-6,6}) for (int z : new int[]{-5,5})
                    for (int y=0; y<=7; y++)
                        world.setBlockState(p.add(x,y,z), y%2==0 ? accent : wall, 2);
                for (int x=-5; x<=5; x+=2)
                    world.setBlockState(p.add(x,7,-6), roof, 2);
            } else if (type == 3) { // Kumo: timber galleries and raised eaves
                for (int x=-6; x<=6; x++) {
                    world.setBlockState(p.add(x,4,-7), accent, 2);
                    world.setBlockState(p.add(x,4,7), accent, 2);
                }
                for (int x : new int[]{-6,6}) for (int z : new int[]{-6,6})
                    for (int y=1; y<=7; y++)
                        world.setBlockState(p.add(x,y,z), wall, 2);
            } else { // Kiri: steep roof gables and dark corner columns
                for (int x : new int[]{-6,6}) for (int z=-6; z<=6; z++)
                    for (int y=1; y<=7; y++)
                        if (z%3==0) world.setBlockState(p.add(x,y,z), accent, 2);
                for (int x=-5; x<=5; x++) {
                    world.setBlockState(p.add(x,7,-6), roof, 2);
                    world.setBlockState(p.add(x,7,6), roof, 2);
                }
            }
        }

        private void buildVillageGate(World world, BlockPos p, IBlockState accent, IBlockState roof) {
            // Gate is centered directly on the northern wall line (z = -105).
            for (int side : new int[]{-1, 1}) {
                int x = side * 6;
                for (int y = 0; y <= 10; y++) {
                    world.setBlockState(p.add(x, y, 0), accent, 2);
                    world.setBlockState(p.add(x + side, y, 0), accent, 2);
                }
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
                    world.setBlockState(p.add(x + dx, 11, dz), roof, 2);
                world.setBlockState(p.add(x, 12, 0), Blocks.GLOWSTONE.getDefaultState(), 2);
            }
            // Raised lintel bridges the two gate towers; the central passage remains open.
            for (int x = -5; x <= 5; x++) {
                world.setBlockState(p.add(x, 8, 0), accent, 2);
                world.setBlockState(p.add(x, 9, 0), roof, 2);
            }
            for (int x = -5; x <= 5; x++) for (int y = 0; y <= 6; y++)
                world.setBlockToAir(p.add(x, y, 0));
            // Paved road passes straight through the gate into the village.
            for (int z = -12; z <= 12; z++) for (int x = -4; x <= 4; x++)
                world.setBlockState(p.add(x, -1, z), Blocks.STONEBRICK.getDefaultState(), 2);
        }

        private void buildVillageGarden(World world, BlockPos p, Random random) {
            // Four small ponds and lamp posts make the center feel lived-in.
            for (int side : new int[]{-1,1}) {
                BlockPos pond=p.add(side*12, -1, side*12);
                for (int dx=-2; dx<=2; dx++) for (int dz=-2; dz<=2; dz++) {
                    if (Math.abs(dx)+Math.abs(dz)<=3) {
                        world.setBlockState(pond.add(dx,0,dz), Blocks.WATER.getDefaultState(), 2);
                        if (Math.abs(dx)+Math.abs(dz)==3)
                            world.setBlockState(pond.add(dx,0,dz), Blocks.STONEBRICK.getDefaultState(), 2);
                    }
                }
            }
            for (int x : new int[]{-8,8}) for (int z : new int[]{-8,8}) {
                for (int y=0; y<=3; y++)
                    world.setBlockState(p.add(x,y,z), Blocks.OAK_FENCE.getDefaultState(), 2);
                world.setBlockState(p.add(x,4,z), Blocks.GLOWSTONE.getDefaultState(), 2);
                world.setBlockState(p.add(x+1,0,z), Blocks.YELLOW_FLOWER.getDefaultState(), 2);
                world.setBlockState(p.add(x-1,0,z), Blocks.RED_FLOWER.getDefaultState(), 2);
            }
        }

        private void spawnVillageResidents(World world, BlockPos center, Random random) {
            // Populate each settlement with seventeen ordinary villagers.
            for (int i=0; i<17; i++) {
                EntityVillager villager = new EntityVillager(world);
                BlockPos at = center.add(random.nextInt(25)-12, 0, random.nextInt(25)-12);
                at = world.getTopSolidOrLiquidBlock(at);
                villager.setPosition(at.getX()+0.5, at.getY(), at.getZ()+0.5);
                world.spawnEntity(villager);
            }

            // Spawn thirty AHZNB ninja/shinobi, cycling available matching entity types.
            java.util.List<EntityEntry> ninjaTypes = new java.util.ArrayList<EntityEntry>();
            for (EntityEntry entry : ForgeRegistries.ENTITIES.getValuesCollection()) {
                ResourceLocation id = entry.getRegistryName();
                if (id == null || !"narutomod".equals(id.getResourceDomain())) continue;
                String path = id.getResourcePath().toLowerCase();
                if (!path.contains("ninja") && !path.contains("shinobi")) continue;
                ninjaTypes.add(entry);
            }
            if (!ninjaTypes.isEmpty()) {
                for (int i=0; i<30; i++) {
                    Entity ninja = ninjaTypes.get(i % ninjaTypes.size()).newInstance(world);
                    if (ninja == null) continue;
                    BlockPos at = world.getTopSolidOrLiquidBlock(center.add(random.nextInt(25)-12, 0, random.nextInt(25)-12));
                    ninja.setPosition(at.getX()+0.5, at.getY(), at.getZ()+0.5);
                    world.spawnEntity(ninja);
                }
            }
        }

        private void buildGardenTree(World world, BlockPos p, Random random) {
            // Small landscaped trees and flower beds around each home.
            int height=3+random.nextInt(2);
            for (int y=0; y<height; y++)
                world.setBlockState(p.add(0,y,0), Blocks.LOG.getDefaultState(), 2);
            for (int dx=-2; dx<=2; dx++) for (int dz=-2; dz<=2; dz++) {
                for (int dy=height-1; dy<=height+1; dy++) {
                    if (Math.abs(dx)+Math.abs(dz)+(dy==height?0:1)<=4)
                        world.setBlockState(p.add(dx,dy,dz), Blocks.LEAVES.getDefaultState(), 2);
                }
            }
            world.setBlockState(p.add(1,0,1), Blocks.YELLOW_FLOWER.getDefaultState(), 2);
            world.setBlockState(p.add(-1,0,-1), Blocks.RED_FLOWER.getDefaultState(), 2);
        }

        private void placeLootChest(World world, BlockPos pos, Random random) {
            world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 2);
            if (world.getTileEntity(pos) instanceof TileEntityChest)
                fillChest((TileEntityChest)world.getTileEntity(pos), random);
        }

        private void fillChest(TileEntityChest chest, Random random) {
            // Each chest gets 10-15 stacks of useful supplies.
            Item[] commonLoot = { Items.BREAD, Items.COOKED_BEEF, Items.COOKED_CHICKEN,
                Items.CARROT, Items.POTATO, Items.APPLE, Items.ARROW, Items.STICK,
                Items.STRING, Items.LEATHER, Items.COAL, Items.IRON_INGOT,
                Items.GOLD_INGOT, Items.EXPERIENCE_BOTTLE, Items.PAPER, Items.BOOK,
                Items.STICK, Items.BONE };
            int stackCount = 10 + random.nextInt(6);
            for (int i=0; i<stackCount; i++) {
                Item item = commonLoot[random.nextInt(commonLoot.length)];
                int amount = 1 + random.nextInt(Math.max(1, Math.min(4, item.getItemStackLimit())));
                put(chest, new ItemStack(item, amount), random);
            }
            // Up to three bonus Naruto items, when valid AHZNB items are configured.
            int added = 0;
            for (String raw : ahznbLoot) {
                if (added >= 3 || random.nextInt(100) >= 10) continue;
                String[] parts = raw.trim().split("@");
                Item item = Item.REGISTRY.getObject(new ResourceLocation(parts[0]));
                if (item != null && item != Items.AIR) {
                    int meta = 0;
                    if (parts.length > 1) { try { meta = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {} }
                    put(chest, new ItemStack(item, 1, meta), random);
                    added++;
                }
            }
        }
        private void put(TileEntityChest chest, ItemStack stack, Random random) {
            for (int i = 0; i < 20; i++) {
                int slot = random.nextInt(chest.getSizeInventory());
                if (chest.getStackInSlot(slot).isEmpty()) {
                    chest.setInventorySlotContents(slot, stack);
                    return;
                }
            }
        }
    }
}
