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

    static int spacingBlocks = 670;
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
            spacingBlocks = cfg.getInt("spacingBlocks", "worldgen", 670, 670, 900,
                    "Approximate target center-to-center spacing in blocks. Default 670; tune between 670 and 900.");
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
            // in a 166-block radius. The terrain block itself is preserved.
            final int radius = 166;
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
                // Five additional homes, positioned within the single perimeter wall
                {-85,-10}, {-55,-82}, {30,-88}, {88,35}, {-5,90}
            };
            for (int i=0; i<homes.length; i++) {
                BlockPos house = p.add(homes[i][0], 0, homes[i][1]);
                buildNinjaHouse(world, house, wall, roof, accent, random, type);
                buildDistinctVillageArchitecture(world, house, wall, roof, accent, type, random);
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
            buildVillageGate(world, p.add(0,0,-110), accent, roof);
            buildVillageGarden(world, p, random);
            spawnBijuu(world, p, random);
            spawnVillageResidents(world, p, random);
        }

        private void buildNinjaHouse(World world, BlockPos p, IBlockState wall, IBlockState roof,
                                     IBlockState accent, Random random, int style) {
            // Large two-storey player-style home with a broad tiered roof.
            IBlockState glass = Blocks.GLASS_PANE.getDefaultState();
            IBlockState fence = Blocks.OAK_FENCE.getDefaultState();
            IBlockState plank = Blocks.PLANKS.getStateFromMeta(0);
            IBlockState slab = Blocks.WOODEN_SLAB.getDefaultState();

            // Wide foundation and lower floor (11 x 11).
            for (int dx=-5; dx<=5; dx++) for (int dz=-5; dz<=5; dz++) {
                world.setBlockState(p.add(dx,-1,dz), accent, 2);
                world.setBlockState(p.add(dx,0,dz), plank, 2);
            }

            // First floor walls, tall windows and a wide front entrance.
            for (int y=1; y<=4; y++) for (int i=-5; i<=5; i++) {
                world.setBlockState(p.add(i,y,-5), wall, 2);
                world.setBlockState(p.add(i,y,5), wall, 2);
                world.setBlockState(p.add(-5,y,i), wall, 2);
                world.setBlockState(p.add(5,y,i), wall, 2);
            }
            for (int y=1; y<=2; y++) {
                world.setBlockToAir(p.add(-1,y,-5));
                world.setBlockToAir(p.add(0,y,-5));
            }
            // Two double-width entrances on opposite facades, facing each other.
            for (int zFace : new int[]{-5,5}) {
                for (int y=1; y<=2; y++) {
                    world.setBlockToAir(p.add(-1,y,zFace));
                    world.setBlockToAir(p.add(0,y,zFace));
                }
                world.setBlockState(p.add(-1,1,zFace), Blocks.OAK_DOOR.getStateFromMeta(0), 2);
                world.setBlockState(p.add(-1,2,zFace), Blocks.OAK_DOOR.getStateFromMeta(8), 2);
                world.setBlockState(p.add(0,1,zFace), Blocks.OAK_DOOR.getStateFromMeta(0), 2);
                world.setBlockState(p.add(0,2,zFace), Blocks.OAK_DOOR.getStateFromMeta(8), 2);
            }
            // Entrance canopy and decorative side pillars.
            for (int dx=-2; dx<=1; dx++) {
                world.setBlockState(p.add(dx,0,-6), accent, 2);
                world.setBlockState(p.add(dx,3,-6), roof, 2);
            }
            for (int y=1; y<=3; y++) {
                world.setBlockState(p.add(-2,y,-5), accent, 2);
                world.setBlockState(p.add(1,y,-5), accent, 2);
            }
            // Large windows on each facade.
            for (int y=2; y<=3; y++) for (int dx : new int[]{-3,-2,2,3}) {
                world.setBlockState(p.add(dx,y,-5), glass, 2);
                world.setBlockState(p.add(dx,y,5), glass, 2);
                world.setBlockState(p.add(-5,y,dx), glass, 2);
                world.setBlockState(p.add(5,y,dx), glass, 2);
            }

            // Heavy timber corner frames and floor band.
            for (int dx : new int[]{-5,5}) for (int dz : new int[]{-5,5})
                for (int y=1; y<=7; y++) world.setBlockState(p.add(dx,y,dz), accent, 2);
            for (int dx=-5; dx<=5; dx++) for (int dz=-5; dz<=5; dz++) {
                if (Math.abs(dx)==5 || Math.abs(dz)==5)
                    world.setBlockState(p.add(dx,4,dz), accent, 2);
            }

            // Upper storey set slightly inward, with balcony.
            for (int dx=-4; dx<=4; dx++) for (int dz=-4; dz<=4; dz++) {
                if (Math.abs(dx)==4 || Math.abs(dz)==4) {
                    world.setBlockState(p.add(dx,5,dz), wall, 2);
                    world.setBlockState(p.add(dx,6,dz), wall, 2);
                }
            }
            for (int y=5; y<=6; y++) {
                world.setBlockToAir(p.add(0,y,-4));
                world.setBlockState(p.add(-2,y,-4), glass, 2);
                world.setBlockState(p.add(2,y,-4), glass, 2);
                world.setBlockState(p.add(-4,y,0), glass, 2);
                world.setBlockState(p.add(4,y,0), glass, 2);
            }
            for (int dx=-5; dx<=5; dx++) {
                world.setBlockState(p.add(dx,5,-6), slab, 2);
                world.setBlockState(p.add(dx,5,6), slab, 2);
            }
            for (int dz=-6; dz<=6; dz++) {
                world.setBlockState(p.add(-6,5,dz), slab, 2);
                world.setBlockState(p.add(6,5,dz), slab, 2);
            }
            for (int dx=-6; dx<=6; dx+=2) {
                world.setBlockState(p.add(dx,6,-6), fence, 2);
                world.setBlockState(p.add(dx,6,6), fence, 2);
            }
            for (int dz=-6; dz<=6; dz+=2) {
                world.setBlockState(p.add(-6,6,dz), fence, 2);
                world.setBlockState(p.add(6,6,dz), fence, 2);
            }

            // Multi-level Japanese-inspired roof with wide eaves.
            for (int layer=0; layer<3; layer++) {
                int radius=(style==1 ? 6-layer : (style==2 ? 6-layer*2 : (style==3 ? 8-layer*2 : 7-layer*2)));
                int y=7+layer;
                for (int dx=-radius; dx<=radius; dx++) for (int dz=-radius; dz<=radius; dz++) {
                    // Filled roof tiers look like a real Japanese tiled roof, not a hollow outline.
                    if (Math.abs(dx)<=radius && Math.abs(dz)<=radius)
                        world.setBlockState(p.add(dx,y,dz), roof, 2);
                }
                for (int i=-radius; i<=radius; i+=2) {
                    world.setBlockState(p.add(i,y+1,-radius), accent, 2);
                    world.setBlockState(p.add(i,y+1,radius), accent, 2);
                }
            }
            for (int dx=-2; dx<=2; dx++) for (int dz=-2; dz<=2; dz++)
                world.setBlockState(p.add(dx,10,dz), roof, 2);
            world.setBlockState(p.add(0,11,0), accent, 2);
            world.setBlockState(p.add(0,12,0), Blocks.GLOWSTONE.getDefaultState(), 2);

            // Clear, flat approach outside both opposing entrances.
            for (int z=-9; z<=-6; z++) for (int dx=-2; dx<=2; dx++)
                world.setBlockToAir(p.add(dx,1,z));
            for (int z=6; z<=9; z++) for (int dx=-2; dx<=2; dx++)
                world.setBlockToAir(p.add(dx,1,z));
            world.setBlockState(p.add(-3,3,-5), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(3,3,-5), Blocks.TORCH.getDefaultState(), 2);
            // Two loot chests placed on opposite sides of the ground floor.
            placeLootChest(world, p.add(-3,1,3), random);
            placeLootChest(world, p.add(3,1,3), random);
            world.setBlockState(p.add(0,3,0), Blocks.GLOWSTONE.getDefaultState(), 2);
            world.setBlockState(p.add(-4,3,0), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(4,3,0), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(0,6,0), Blocks.GLOWSTONE.getDefaultState(), 2);
            world.setBlockState(p.add(-3,6,0), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(3,6,0), Blocks.TORCH.getDefaultState(), 2);
            // Ten dedicated upper-floor lights, evenly distributed around the second-storey interior.
            int[][] upperLights = {{-3,1},{-1,1},{1,1},{3,1},{-3,-1},{-1,-1},{1,-1},{3,-1},{-2,3},{2,3}};
            for (int[] light : upperLights) {
                world.setBlockState(p.add(light[0],6,light[1]), Blocks.GLOWSTONE.getDefaultState(), 2);
            }


            // Two separate beds, plus work area, shelves and carpet.
            world.setBlockState(p.add(-3,1,2), Blocks.BED.getStateFromMeta(0), 2);
            world.setBlockState(p.add(-3,1,3), Blocks.BED.getStateFromMeta(8), 2);
            world.setBlockState(p.add(3,1,-3), Blocks.BED.getStateFromMeta(0), 2);
            world.setBlockState(p.add(3,1,-2), Blocks.BED.getStateFromMeta(8), 2);
            world.setBlockState(p.add(-3,1,-2), Blocks.CRAFTING_TABLE.getDefaultState(), 2);
            world.setBlockState(p.add(-2,1,-2), Blocks.FURNACE.getDefaultState(), 2);
            world.setBlockState(p.add(2,1,-2), Blocks.BOOKSHELF.getDefaultState(), 2);
            world.setBlockState(p.add(2,1,-1), Blocks.BOOKSHELF.getDefaultState(), 2);
            world.setBlockState(p.add(0,1,2), Blocks.CARPET.getDefaultState(), 2);
            world.setBlockState(p.add(1,1,2), Blocks.FLOWER_POT.getDefaultState(), 2);
        }

        private void buildCentralResidence(World world, BlockPos p, IBlockState wall, IBlockState roof, IBlockState accent, Random random) {
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
            int radius = 110;
            int previousX = 0, previousZ = -radius;
            for (int step = 1; step <= 3600; step++) {
                double angle = Math.toRadians(step / 10.0);
                int x = (int)Math.round(Math.cos(angle) * radius);
                int z = (int)Math.round(Math.sin(angle) * radius);
                drawWallSegment(world, p, previousX, previousZ, x, z, wall, accent);
                previousX = x;
                previousZ = z;
            }
            // Re-open a clean 11-block gate passage after rasterizing the ring.
            for (int x = -5; x <= 5; x++) for (int y = 0; y <= 6; y++)
                world.setBlockToAir(p.add(x, y, -110));
        }

        private void drawWallSegment(World world, BlockPos p, int x1, int z1, int x2, int z2,
                                     IBlockState wall, IBlockState accent) {
            int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
            for (int i = 0; i <= steps; i++) {
                double t = steps == 0 ? 0.0 : i / (double)steps;
                int x = (int)Math.round(x1 + (x2 - x1) * t);
                int z = (int)Math.round(z1 + (z2 - z1) * t);
                for (int y = 0; y <= 5; y++)
                    world.setBlockState(p.add(x, y, z), y == 5 ? accent : wall, 2);
                if ((x + z) % 7 == 0)
                    world.setBlockState(p.add(x, 6, z), accent, 2);
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
            // Wall radius is 110 blocks; place the beast at radius 145 (35 blocks beyond the wall).
            double angle = random.nextDouble() * Math.PI * 2.0;
            int distance = 145;
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
            // Gate is centered directly on the northern wall line (z = -110).
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
