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
            double chancePerChunk = Math.min(1.0, 2400.0 / (spacingBlocks * (double) spacingBlocks));
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

            // Fifteen homes; the central residence is the sixteenth main building.
            int[][] homes = {
                {-29,-27}, {-9,-34}, {14,-34}, {34,-22}, {37,2},
                {29,29}, {6,37}, {-19,32}, {-37,12}, {-35,-13},
                {-65,-45}, {-42,-68}, {-5,-72}, {38,-66}, {68,-42}
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
            // A more legible village center: stone plaza, radial lanes and decorative lamps.
            buildVillagePlaza(world, p, wall, accent);
            buildVillageWalls(world, p, wall, accent);
            buildVillageGate(world, p.add(0,0,-92), accent, roof);
            // Four watchtowers reinforce the settlement's defensive silhouette.
            buildWatchtower(world, p.add(-78,0,-78), wall, roof, accent);
            buildWatchtower(world, p.add(78,0,-78), wall, roof, accent);
            buildWatchtower(world, p.add(-78,0,78), wall, roof, accent);
            buildWatchtower(world, p.add(78,0,78), wall, roof, accent);
            // Four tall sea-watch towers stand beyond the defensive wall.
            buildSeaWatchtower(world, p.add(0,0,-148), wall, roof, accent);
            buildSeaWatchtower(world, p.add(148,0,0), wall, roof, accent);
            buildSeaWatchtower(world, p.add(0,0,148), wall, roof, accent);
            buildSeaWatchtower(world, p.add(-148,0,0), wall, roof, accent);
            buildVillageGarden(world, p, random);
            // Separate academy near the inner side of the southern wall.
            buildNinjutsuAcademy(world, p.add(-52, 0, 66), wall, roof, accent, random);
            // A distinct Uchiha district, outside the main village wall.
            buildUchihaDistrict(world, p.add(300, 0, 0), random);
            // Rare chakra tree: one chance in eight per generated ninja village.
            if (random.nextInt(8) == 0) buildChakraTree(world, p.add(52, 0, 55), random);
            spawnBijuu(world, p, random);
            spawnVillageResidents(world, p, random);
        }

        private void buildNinjaHouse(World world, BlockPos p, IBlockState wall, IBlockState roof,
                                     IBlockState accent, Random random, int style) {
            // Rebuilt as a coherent Japanese shinobi townhouse: stone plinth,
            // timber frame, bright shoji windows, a real entrance porch and a pagoda roof.
            IBlockState wood = style==1 ? Blocks.PLANKS.getStateFromMeta(2) :
                               style==2 ? Blocks.PLANKS.getStateFromMeta(1) : Blocks.PLANKS.getDefaultState();
            IBlockState beam = style==1 ? Blocks.LOG.getDefaultState() : Blocks.LOG.getStateFromMeta(1);
            IBlockState glass = Blocks.STAINED_GLASS_PANE.getStateFromMeta(
                style==0 ? 0 : style==1 ? 4 : style==2 ? 7 : style==3 ? 11 : 15);
            IBlockState stone = style==1 ? Blocks.SANDSTONE.getDefaultState() : Blocks.STONEBRICK.getDefaultState();
            IBlockState slab = style==1 ? Blocks.WOODEN_SLAB.getStateFromMeta(2) : Blocks.WOODEN_SLAB.getDefaultState();
            int w=6, d=6;

            // Foundation and continuous floor.
            for (int x=-7;x<=7;x++) for (int z=-7;z<=7;z++) {
                world.setBlockState(p.add(x,-1,z), stone, 2);
                if (Math.abs(x)<=w && Math.abs(z)<=d)
                    world.setBlockState(p.add(x,0,z), wood, 2);
            }

            // Solid lower walls, timber posts and top/bottom framing.
            for (int y=1;y<=4;y++) for (int x=-w;x<=w;x++) for (int z=-d;z<=d;z++)
                if (Math.abs(x)==w || Math.abs(z)==d)
                    world.setBlockState(p.add(x,y,z), wall, 2);
            for (int x : new int[]{-6,-3,0,3,6}) for (int y=1;y<=5;y++) {
                world.setBlockState(p.add(x,y,-6), beam, 2);
                world.setBlockState(p.add(x,y,6), beam, 2);
            }
            for (int z : new int[]{-6,-3,0,3,6}) for (int y=1;y<=5;y++) {
                world.setBlockState(p.add(-6,y,z), beam, 2);
                world.setBlockState(p.add(6,y,z), beam, 2);
            }
            for (int x=-5;x<=5;x++) {
                world.setBlockState(p.add(x,1,-6), wood, 2);
                world.setBlockState(p.add(x,4,-6), beam, 2);
                world.setBlockState(p.add(x,1,6), wood, 2);
                world.setBlockState(p.add(x,4,6), beam, 2);
            }

            // Four broad shoji window panels with timber mullions.
            for (int y=2;y<=3;y++) for (int n : new int[]{-4,-3,-2,2,3,4}) {
                world.setBlockState(p.add(n,y,-6), glass, 2);
                world.setBlockState(p.add(n,y,6), glass, 2);
                world.setBlockState(p.add(-6,y,n), glass, 2);
                world.setBlockState(p.add(6,y,n), glass, 2);
            }

            // Front opening remains completely walkable; door, steps and lantern porch.
            world.setBlockToAir(p.add(0,1,6));
            world.setBlockToAir(p.add(0,2,6));
            world.setBlockState(p.add(0,1,6), Blocks.OAK_DOOR.getStateFromMeta(0), 2);
            world.setBlockState(p.add(0,2,6), Blocks.OAK_DOOR.getStateFromMeta(8), 2);
            for (int x=-2;x<=2;x++) {
                world.setBlockState(p.add(x,0,7), stone, 2);
                world.setBlockState(p.add(x,0,8), stone, 2);
                world.setBlockState(p.add(x,1,8), slab, 2);
            }
            for (int x : new int[]{-3,3}) {
                for (int y=1;y<=3;y++) world.setBlockState(p.add(x,y,7), beam, 2);
                world.setBlockState(p.add(x,4,7), Blocks.GLOWSTONE.getDefaultState(), 2);
                world.setBlockState(p.add(x,5,7), Blocks.TRAPDOOR.getDefaultState(), 2);
            }
            for (int x=-4;x<=4;x++) {
                world.setBlockState(p.add(x,3,8), beam, 2);
                world.setBlockState(p.add(x,4,8), roof, 2);
            }
            for (int x : new int[]{-4,4}) for (int y=2;y<=4;y++)
                world.setBlockState(p.add(x,y,8), beam, 2);

            // Solid stepped pagoda roof: every course is filled, and each
            // successive course shrinks by one block on all sides. This removes
            // the empty gaps between the visible rings and uses only 1.12.2 blocks.
            for (int step=0;step<6;step++) {
                int radius=9-step;
                int y=5+step;
                IBlockState course = (step==0 || step==2 || step==4) ? roof : slab;
                for (int x=-radius;x<=radius;x++) for (int z=-radius;z<=radius;z++) {
                    world.setBlockState(p.add(x,y,z), course, 2);
                }
            }
            // Raised timber trim follows the outside edge of the broad eaves.
            for (int n=-8;n<=8;n++) {
                world.setBlockState(p.add(n,6,-9), beam, 2);
                world.setBlockState(p.add(n,6,9), beam, 2);
                world.setBlockState(p.add(-9,6,n), beam, 2);
                world.setBlockState(p.add(9,6,n), beam, 2);
            }
            for (int x=-3;x<=3;x++) {
                world.setBlockState(p.add(x,10,0), beam, 2);
                if (Math.abs(x)==3) world.setBlockState(p.add(x,11,0), accent, 2);
            }
            for (int z=-2;z<=2;z++) world.setBlockState(p.add(0,10,z), accent, 2);

            // Decorative Japanese townhouse details: second-level balcony rails,
            // corner brackets, roof-end ornaments and warm lanterns.
            for (int x=-5;x<=5;x++) {
                if (x%2==0) {
                    world.setBlockState(p.add(x,4,8), Blocks.OAK_FENCE.getDefaultState(), 2);
                    world.setBlockState(p.add(x,4,-8), Blocks.OAK_FENCE.getDefaultState(), 2);
                }
            }
            for (int z=-5;z<=5;z+=2) {
                world.setBlockState(p.add(8,4,z), Blocks.OAK_FENCE.getDefaultState(), 2);
                world.setBlockState(p.add(-8,4,z), Blocks.OAK_FENCE.getDefaultState(), 2);
            }
            for (int x : new int[]{-7,7}) for (int z : new int[]{-7,7}) {
                for (int y=5;y<=7;y++)
                    world.setBlockState(p.add(x,y,z), beam, 2);
                world.setBlockState(p.add(x,8,z), accent, 2);
            }
            // Hanging lantern-like lights beneath the eaves.
            for (int x : new int[]{-5,0,5}) for (int z : new int[]{-5,5}) {
                world.setBlockState(p.add(x,4,z), Blocks.OAK_FENCE.getDefaultState(), 2);
                world.setBlockState(p.add(x,3,z), Blocks.GLOWSTONE.getDefaultState(), 2);
                world.setBlockState(p.add(x,2,z), Blocks.TRAPDOOR.getDefaultState(), 2);
            }
            // Decorative roof ridge and raised corner tips.
            for (int x=-4;x<=4;x++) {
                world.setBlockState(p.add(x,11,0), x%2==0 ? accent : beam, 2);
            }
            for (int x : new int[]{-9,9}) for (int z : new int[]{-9,9}) {
                world.setBlockState(p.add(x,6,z), accent, 2);
                world.setBlockState(p.add(x,7,z), roof, 2);
            }

            // Interior: clear central path, cozy furnishings, and reachable treasure chest.
            for (int z=-5;z<=5;z++) {
                world.setBlockToAir(p.add(0,1,z));
                world.setBlockToAir(p.add(0,2,z));
            }
            world.setBlockState(p.add(-4,1,-3), Blocks.BOOKSHELF.getDefaultState(), 2);
            world.setBlockState(p.add(-3,1,-3), Blocks.BOOKSHELF.getDefaultState(), 2);
            world.setBlockState(p.add(4,1,-3), Blocks.CRAFTING_TABLE.getDefaultState(), 2);
            world.setBlockState(p.add(4,1,-2), Blocks.FURNACE.getDefaultState(), 2);
            world.setBlockState(p.add(-4,1,2), Blocks.BED.getStateFromMeta(0), 2);
            world.setBlockState(p.add(-4,1,3), Blocks.BED.getStateFromMeta(8), 2);
            placeLootChest(world, p.add(2,1,-3), random);
            world.setBlockState(p.add(2,2,-3), Blocks.AIR.getDefaultState(), 2);

            // Compact garden details outside the entrance, without blocking the approach.
            for (int x : new int[]{-5,5}) {
                world.setBlockState(p.add(x,0,9), stone, 2);
                world.setBlockState(p.add(x,1,9), Blocks.FLOWER_POT.getDefaultState(), 2);
                world.setBlockState(p.add(x,2,9), Blocks.YELLOW_FLOWER.getDefaultState(), 2);
            }
            for (int x=-3;x<=3;x+=2) {
                world.setBlockState(p.add(x,0,10), (x==0 ? stone : Blocks.GRASS.getDefaultState()), 2);
                if (x!=0) world.setBlockState(p.add(x,1,10), Blocks.RED_FLOWER.getDefaultState(), 2);
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
            int radius = 92;
            int previousX = 0, previousZ = -radius;
            for (int step = 1; step <= 3600; step++) {
                double angle = Math.toRadians(step / 10.0);
                int x = (int)Math.round(Math.cos(angle) * radius);
                int z = (int)Math.round(Math.sin(angle) * radius);
                drawWallSegment(world, p, previousX, previousZ, x, z, wall, accent);
                previousX = x;
                previousZ = z;
            }
            // Re-open a clean 11-block-wide passage through the one-block wall.
            for (int x = -5; x <= 5; x++) for (int y = 0; y <= 6; y++)
                world.setBlockToAir(p.add(x, y, -92));
        }

        private void drawWallSegment(World world, BlockPos p, int x1, int z1, int x2, int z2,
                                     IBlockState wall, IBlockState accent) {
            int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
            for (int i = 0; i <= steps; i++) {
                double t = steps == 0 ? 0.0 : i / (double)steps;
                int x = (int)Math.round(x1 + (x2 - x1) * t);
                int z = (int)Math.round(z1 + (z2 - z1) * t);
                // One-block-thick continuous wall, seven blocks tall (Y=0..6).
                for (int y = 0; y <= 6; y++) {
                    IBlockState course = (y == 6) ? accent : wall;
                    world.setBlockState(p.add(x, y, z), course, 2);
                }
                // Stone merlons along the top give the wall a fortified silhouette.
                if ((x + z) % 5 == 0) {
                    world.setBlockState(p.add(x, 7, z), accent, 2);
                }
            }
        }

        private void spawnBijuu(World world, BlockPos center, Random random) {
            // 100% chance per generated village: spawn one tailed beast at the village center.
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
            // Place the beast at the central plaza, not outside the defensive wall.
            BlockPos spawn = world.getTopSolidOrLiquidBlock(center);
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
            // Gate is centered directly on the northern wall line (z = -92).
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
            for (int x = -5; x <= 5; x++) for (int y = 0; y <= 6; y++) {
                world.setBlockToAir(p.add(x, y, 0));
                world.setBlockToAir(p.add(x, y, 1));
            }
            // Paved road passes straight through the gate into the village.
            for (int z = -12; z <= 12; z++) for (int x = -4; x <= 4; x++)
                world.setBlockState(p.add(x, -1, z), Blocks.STONEBRICK.getDefaultState(), 2);
        }

        private void buildVillagePlaza(World world, BlockPos p, IBlockState wall, IBlockState accent) {
            // A patterned 25x25 central square with a raised monument in the middle.
            for (int x=-12; x<=12; x++) for (int z=-12; z<=12; z++) {
                boolean border = Math.abs(x)==12 || Math.abs(z)==12;
                boolean motif = (Math.abs(x)==6 && Math.abs(z)<=6) ||
                                (Math.abs(z)==6 && Math.abs(x)<=6);
                IBlockState floor = border || motif ? accent :
                    ((Math.abs(x)+Math.abs(z))%2==0 ? Blocks.STONEBRICK.getDefaultState() :
                                                        Blocks.STONE.getDefaultState());
                world.setBlockState(p.add(x,-1,z), floor, 2);
            }
            // Four low corner planters and a central monument.
            for (int sx : new int[]{-9,9}) for (int sz : new int[]{-9,9}) {
                for (int dx=-1; dx<=1; dx++) for (int dz=-1; dz<=1; dz++)
                    world.setBlockState(p.add(sx+dx,0,sz+dz), accent, 2);
                world.setBlockState(p.add(sx,1,sz), Blocks.LEAVES.getDefaultState(), 2);
                world.setBlockState(p.add(sx,2,sz), Blocks.LEAVES.getDefaultState(), 2);
                world.setBlockState(p.add(sx+1,1,sz), Blocks.YELLOW_FLOWER.getDefaultState(), 2);
            }
            for (int y=0; y<=3; y++) {
                world.setBlockState(p.add(0,y,0), y==3 ? Blocks.GLOWSTONE.getDefaultState() : accent, 2);
                world.setBlockState(p.add(1,y,0), accent, 2);
                world.setBlockState(p.add(-1,y,0), accent, 2);
                world.setBlockState(p.add(0,y,1), accent, 2);
                world.setBlockState(p.add(0,y,-1), accent, 2);
            }
            // Symmetric lantern posts around the square.
            for (int x : new int[]{-11,11}) for (int z : new int[]{-11,11})
                buildStreetLamp(world, p.add(x,0,z), accent);
        }

        private void buildStreetLamp(World world, BlockPos p, IBlockState accent) {
            for (int y=0; y<=4; y++)
                world.setBlockState(p.add(0,y,0), y==0 ? accent : Blocks.OAK_FENCE.getDefaultState(), 2);
            world.setBlockState(p.add(0,5,0), Blocks.GLOWSTONE.getDefaultState(), 2);
            for (int dx=-1; dx<=1; dx++) for (int dz=-1; dz<=1; dz++)
                if (Math.abs(dx)+Math.abs(dz)==1)
                    world.setBlockState(p.add(dx,4,dz), accent, 2);
        }

        private void buildWatchtower(World world, BlockPos p, IBlockState wall, IBlockState roof, IBlockState accent) {
            // Four-level open defensive tower with viewing slits and a broad roof.
            for (int y=0; y<=10; y++) {
                for (int x=-3; x<=3; x++) for (int z=-3; z<=3; z++) {
                    boolean corner = Math.abs(x)==3 && Math.abs(z)==3;
                    boolean edge = Math.abs(x)==3 || Math.abs(z)==3;
                    if (y<=7 && edge && (corner || y==0 || y==7 || (y>=3 && y<=5)))
                        world.setBlockState(p.add(x,y,z), (corner || y==0 || y==7) ? accent : wall, 2);
                }
            }
            // Reinforced upper gallery and pagoda-like stepped roof.
            for (int x=-4; x<=4; x++) for (int z=-4; z<=4; z++) {
                if (Math.abs(x)==4 || Math.abs(z)==4)
                    world.setBlockState(p.add(x,8,z), accent, 2);
                world.setBlockState(p.add(x,9,z), roof, 2);
            }
            for (int x=-3; x<=3; x++) for (int z=-3; z<=3; z++)
                world.setBlockState(p.add(x,10,z), roof, 2);
            for (int x=-2; x<=2; x++) for (int z=-2; z<=2; z++)
                world.setBlockState(p.add(x,11,z), roof, 2);
            world.setBlockState(p.add(0,12,0), Blocks.GLOWSTONE.getDefaultState(), 2);
            // Lit corners make towers visible at night.
            for (int x : new int[]{-2,2}) for (int z : new int[]{-2,2})
                world.setBlockState(p.add(x,6,z), Blocks.TORCH.getDefaultState(), 2);
        }

        private void buildSeaWatchtower(World world, BlockPos p, IBlockState wall, IBlockState roof, IBlockState accent) {
            // Monumental lighthouse-like shinobi sea tower with a broad stone podium,
            // twin observation decks, corner buttresses and a beacon crown.
            for (int x=-7; x<=7; x++) for (int z=-7; z<=7; z++) {
                boolean edge=Math.abs(x)==7 || Math.abs(z)==7;
                if (edge || (Math.abs(x)<=2 && Math.abs(z)<=2))
                    world.setBlockState(p.add(x,0,z), edge ? Blocks.STONEBRICK.getDefaultState() : accent, 2);
            }
            for (int y=1; y<=17; y++) {
                int radius = y<5 ? 4 : 3;
                for (int x=-radius; x<=radius; x++) for (int z=-radius; z<=radius; z++) {
                    if (Math.abs(x)==radius || Math.abs(z)==radius) {
                        boolean window = y>=7 && y<=14 &&
                            ((x==0 && Math.abs(z)==radius) || (z==0 && Math.abs(x)==radius));
                        world.setBlockState(p.add(x,y,z), window ?
                            Blocks.STAINED_GLASS_PANE.getStateFromMeta(3) :
                            ((y%4==0 || y==1) ? accent : wall), 2);
                    }
                }
            }
            // Corner buttresses and two full wraparound lookout decks.
            for (int x : new int[]{-5,5}) for (int z : new int[]{-5,5})
                for (int y=1; y<=16; y++)
                    world.setBlockState(p.add(x,y,z), y%4==0 ? roof : accent, 2);
            for (int level : new int[]{6,13}) {
                for (int x=-6; x<=6; x++) for (int z=-6; z<=6; z++)
                    if (Math.abs(x)==6 || Math.abs(z)==6)
                        world.setBlockState(p.add(x,level,z), accent, 2);
                for (int x=-5; x<=5; x+=2) {
                    world.setBlockState(p.add(x,level+1,-6), Blocks.OAK_FENCE.getDefaultState(), 2);
                    world.setBlockState(p.add(x,level+1,6), Blocks.OAK_FENCE.getDefaultState(), 2);
                }
            }
            // Three dramatic pagoda eaves, each wider than the tower shaft.
            for (int tier=0; tier<3; tier++) {
                int radius=8-tier*2;
                int y=18+tier*2;
                for (int x=-radius; x<=radius; x++) for (int z=-radius; z<=radius; z++) {
                    if (Math.abs(x)>=radius-1 || Math.abs(z)>=radius-1)
                        world.setBlockState(p.add(x,y,z), roof, 2);
                }
                for (int x : new int[]{-radius,radius}) for (int z : new int[]{-radius,radius}) {
                    world.setBlockState(p.add(x,y+1,z), accent, 2);
                    world.setBlockState(p.add(x,y+2,z), roof, 2);
                }
            }
            for (int y=18; y<=25; y++)
                world.setBlockState(p.add(0,y,0), accent, 2);
            world.setBlockState(p.add(0,26,0), Blocks.GLOWSTONE.getDefaultState(), 2);
            for (int x : new int[]{-2,2}) for (int z : new int[]{-2,2})
                world.setBlockState(p.add(x,15,z), Blocks.GLOWSTONE.getDefaultState(), 2);
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

        private void buildNinjutsuAcademy(World world, BlockPos p, IBlockState wall, IBlockState roof,
                                             IBlockState accent, Random random) {
            // Large two-storey academy placed beside the inner village wall.
            for (int x=-12; x<=12; x++) for (int z=-15; z<=15; z++) {
                world.setBlockState(p.add(x,-1,z), Blocks.STONEBRICK.getDefaultState(), 2);
                world.setBlockState(p.add(x,0,z), Blocks.PLANKS.getDefaultState(), 2);
            }
            for (int y=1; y<=8; y++) for (int x=-11; x<=11; x++) for (int z=-14; z<=14; z++) {
                if (Math.abs(x)==11 || Math.abs(z)==14)
                    world.setBlockState(p.add(x,y,z), wall, 2);
            }
            // Clear interior and create a second floor with an open central hall.
            for (int y=1; y<=7; y++) for (int x=-10; x<=10; x++) for (int z=-13; z<=13; z++)
                world.setBlockToAir(p.add(x,y,z));
            for (int x=-10; x<=10; x++) for (int z=-13; z<=13; z++)
                if (Math.abs(x)==10 || Math.abs(z)==13)
                    for (int y=1; y<=8; y++) world.setBlockState(p.add(x,y,z), wall, 2);
            for (int x=-10; x<=10; x++) for (int z=-13; z<=13; z++)
                world.setBlockState(p.add(x,4,z), Blocks.PLANKS.getDefaultState(), 2);
            for (int x=-12; x<=12; x++) for (int z=-15; z<=15; z++)
                if (Math.abs(x)>=10 || Math.abs(z)>=13)
                    world.setBlockState(p.add(x,9,z), roof, 2);
            for (int x=-8; x<=8; x+=4) {
                world.setBlockState(p.add(x,2,-14), Blocks.GLASS_PANE.getDefaultState(), 2);
                world.setBlockState(p.add(x,6,-14), Blocks.GLASS_PANE.getDefaultState(), 2);
            }
            // Main entrance, sign-like crest and training posts.
            for (int y=1; y<=3; y++) for (int x=-2; x<=2; x++)
                world.setBlockToAir(p.add(x,y,-14));
            world.setBlockState(p.add(0,10,0), accent, 2);
            for (int x=-6; x<=6; x+=3) {
                world.setBlockState(p.add(x,1,8), Blocks.FENCE.getDefaultState(), 2);
                world.setBlockState(p.add(x,2,8), Blocks.FENCE.getDefaultState(), 2);
            }
            placeLootChest(world, p.add(-8,1,-10), random);
            placeLootChest(world, p.add(8,1,-10), random);
            placeLootChest(world, p.add(-8,5,10), random);
            placeLootChest(world, p.add(8,5,10), random);
            placeLootChest(world, p.add(0,1,11), random);
        }

        private void buildUchihaDistrict(World world, BlockPos center, Random random) {
            IBlockState dark = Blocks.PLANKS.getStateFromMeta(1);
            IBlockState roof = Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(14);
            int[][] houses = {{-25,-20},{0,-27},{25,-20},{-18,18},{18,18}};
            for (int i=0; i<houses.length; i++) {
                BlockPos h = center.add(houses[i][0],0,houses[i][1]);
                buildNinjaHouse(world,h,dark,roof,Blocks.LOG.getStateFromMeta(1),random,2);
                placeLootChest(world,h.add(0,1,0),random);
            }
            // Uchiha district emblem at its own central square.
            for (int x=-5; x<=5; x++) for (int z=-5; z<=5; z++)
                world.setBlockState(center.add(x,-1,z),
                    (Math.abs(x)==5 || Math.abs(z)==5) ? roof : Blocks.STONEBRICK.getDefaultState(),2);
            // Ten real Uchiha NPCs from Dynamic Dojutsu. Most are ordinary clan members;
            // named/elite Uchiha are deliberately rare.
            String[] uchihaRoster = {
                "dynamicdojutsu:default_uchiha", "dynamicdojutsu:default_uchiha",
                "dynamicdojutsu:default_uchiha", "dynamicdojutsu:default_uchiha",
                "dynamicdojutsu:default_uchiha", "dynamicdojutsu:default_uchiha",
                "dynamicdojutsu:default_uchiha", "dynamicdojutsu:naka_uchiha",
                "dynamicdojutsu:rai_uchiha", "dynamicdojutsu:baru_uchiha"
            };
            for (int i=0; i<uchihaRoster.length; i++) {
                EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(uchihaRoster[i]));
                if (entry == null) continue; // Dynamic Dojutsu is an optional dependency.
                Entity npc = entry.newInstance(world);
                if (npc == null) continue;
                BlockPos at = world.getTopSolidOrLiquidBlock(center.add((i%5-2)*5,0,(i/5==0?-8:8)));
                npc.setPosition(at.getX()+0.5,at.getY(),at.getZ()+0.5);
                world.spawnEntity(npc);
            }
            // One elite Uchiha may appear instead of one ordinary clan member (10% chance).
            if (random.nextInt(10) == 0) {
                EntityEntry elite = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("dynamicdojutsu:madara_uchiha"));
                if (elite != null) {
                    Entity npc = elite.newInstance(world);
                    if (npc != null) {
                        BlockPos at = world.getTopSolidOrLiquidBlock(center.add(0,0,13));
                        npc.setPosition(at.getX()+0.5,at.getY(),at.getZ()+0.5);
                        world.spawnEntity(npc);
                    }
                }
            }
        }

        private void buildChakraTree(World world, BlockPos p, Random random) {
            // Rare landmark tree; its fruit is a renamed golden apple, not an invented mod ID.
            for (int y=0; y<9; y++)
                world.setBlockState(p.add(0,y,0), Blocks.LOG.getDefaultState(), 2);
            for (int y=6; y<=11; y++) {
                int radius = y==11 ? 2 : (y==10 ? 4 : 5);
                for (int x=-radius; x<=radius; x++) for (int z=-radius; z<=radius; z++)
                    if (x*x+z*z <= radius*radius+2 && !(x==0 && z==0))
                        world.setBlockState(p.add(x,y,z), Blocks.LEAVES.getDefaultState(), 2);
            }
            world.setBlockState(p.add(0,12,0), Blocks.GLOWSTONE.getDefaultState(), 2);
            BlockPos chestPos=p.add(2,1,0);
            world.setBlockState(chestPos,Blocks.CHEST.getDefaultState(),2);
            if (world.getTileEntity(chestPos) instanceof TileEntityChest) {
                ItemStack fruit=new ItemStack(Items.GOLDEN_APPLE,1);
                fruit.setStackDisplayName("Chakra Fruit");
                ((TileEntityChest)world.getTileEntity(chestPos)).setInventorySlotContents(13,fruit);
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
            // Exactly 20 occupied slots: 10 vanilla, 5 AHZNB and 5 Dynamic Dojutsu.
            Item[] commonLoot = { Items.BREAD, Items.COOKED_BEEF, Items.COOKED_CHICKEN,
                Items.CARROT, Items.POTATO, Items.APPLE, Items.ARROW, Items.STRING,
                Items.IRON_INGOT, Items.GOLD_INGOT };
            for (Item item : commonLoot) {
                int amount = 1 + random.nextInt(Math.max(1, Math.min(4, item.getItemStackLimit())));
                put(chest, new ItemStack(item, amount), random);
            }
            String[] dynamicLoot = {
                "dynamicdojutsu:typhoon_release", "dynamicdojutsu:dark_release",
                "dynamicdojutsu:steel_release", "dynamicdojutsu:izanagi_scroll",
                "dynamicdojutsu:totsuka_blade"
            };
            addConfiguredLoot(chest, ahznbLoot, 5, random);
            addConfiguredLoot(chest, dynamicLoot, 5, random);
        }

        private void addConfiguredLoot(TileEntityChest chest, String[] loot, int count, Random random) {
            java.util.List<ItemStack> valid = new java.util.ArrayList<ItemStack>();
            for (String raw : loot) {
                String[] parts = raw.trim().split("@");
                if (parts.length == 0 || parts[0].isEmpty()) continue;
                Item item = Item.REGISTRY.getObject(new ResourceLocation(parts[0]));
                if (item == null || item == Items.AIR) continue;
                int meta = 0;
                if (parts.length > 1) {
                    try { meta = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
                }
                valid.add(new ItemStack(item, 1, meta));
            }
            java.util.Collections.shuffle(valid, random);
            for (int i=0; i<Math.min(count, valid.size()); i++)
                put(chest, valid.get(i), random);
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
