package com.example.ahznbminivillages;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;
import java.util.Random;

@Mod(modid = AHZNBMiniVillages.MODID, name = "AHZNB Mini Villages", version = "1.0.0",
     dependencies = "after:ahznb")
public class AHZNBMiniVillages {
    public static final String MODID = "ahznbminivillages";
    static int spacingBlocks = 300;
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
            spacingBlocks = cfg.getInt("spacingBlocks", "worldgen", 300, 200, 500,
                    "Approximate target spacing in blocks. Set between 200 and 500.");
            ahznbLoot = cfg.getStringList("ahznbLoot", "loot", ahznbLoot,
                    "Exact AHZNB item registry IDs. Remove entries here to disable them from village chest loot.");
        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
        GameRegistry.registerWorldGenerator(new MiniVillageGenerator(), 10);
    }

    private static class MiniVillageGenerator implements IWorldGenerator {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world,
                             IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
            if (world.isRemote || world.provider.getDimension() != 0) return;

            // 2.5x the previous attempt rate (256 -> 640).
            double chancePerChunk = Math.min(1.0, 640.0 / (spacingBlocks * (double) spacingBlocks));
            if (random.nextDouble() >= chancePerChunk) return;

            int x = chunkX * 16 + 3 + random.nextInt(10);
            int z = chunkZ * 16 + 3 + random.nextInt(10);
            BlockPos surface = world.getHeight(new BlockPos(x, 0, z));
            int y = surface.getY();
            // Supports default Superflat (surface is usually around Y=4) as well as normal terrain.
            if (y < 4 || y > 250) return;
            if (!isMostlyClear(world, new BlockPos(x, y, z))) return;

            int villageType = Math.floorMod((chunkX * 73428767) ^ (chunkZ * 912931), 5);
            buildVillage(world, new BlockPos(x, y, z), villageType, random);
        }

        private boolean isMostlyClear(World world, BlockPos p) {
            // Sample the area instead of checking every block to keep world generation light.
            for (int dx = -40; dx <= 40; dx += 8) {
                for (int dz = -40; dz <= 40; dz += 8) {
                    Block b = world.getBlockState(p.add(dx, -1, dz)).getBlock();
                    if (b != Blocks.GRASS && b != Blocks.DIRT && b != Blocks.SAND &&
                        b != Blocks.STONE && b != Blocks.GRAVEL) return false;
                    if (!world.isAirBlock(p.add(dx, 1, dz))) return false;
                }
            }
            return true;
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

            // Ten spacious homes arranged around a broad village center.
            int[][] homes = {
                {-24,-24}, {-7,-31}, {12,-30}, {29,-18}, {32,3},
                {23,25}, {4,32}, {-17,27}, {-32,9}, {-30,-12}
            };
            for (int[] home : homes) {
                BlockPos house = p.add(home[0], 0, home[1]);
                buildNinjaHouse(world, house, wall, roof, accent, random);
                buildGardenTree(world, house.add(-8, 0, 5), random);
                buildGardenTree(world, house.add(8, 0, 5), random);
            }

            // A simple gravel path and small central meeting point.
            for (int z = -15; z <= 15; z++) {
                world.setBlockState(p.add(0, -1, z), Blocks.GRAVEL.getDefaultState(), 2);
                world.setBlockState(p.add(1, -1, z), Blocks.GRAVEL.getDefaultState(), 2);
            }
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                world.setBlockState(p.add(dx, -1, dz), Blocks.STONEBRICK.getDefaultState(), 2);
            }
        }

        private void buildNinjaHouse(World world, BlockPos p, IBlockState wall, IBlockState roof,
                                     IBlockState accent, Random random) {
            // Large two-storey player-style home with a broad tiered roof.
            IBlockState glass = Blocks.GLASS_PANE.getDefaultState();
            IBlockState fence = Blocks.OAK_FENCE.getDefaultState();
            IBlockState plank = Blocks.PLANKS.getStateFromMeta(0);
            IBlockState slab = Blocks.WOODEN_SLAB.getDefaultState();
            IBlockState stair = Blocks.OAK_STAIRS.getDefaultState();

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
                for (int dx=-1; dx<=1; dx++) world.setBlockToAir(p.add(dx,y,-5));
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
                int radius=6-layer*2;
                int y=7+layer;
                for (int dx=-radius; dx<=radius; dx++) for (int dz=-radius; dz<=radius; dz++) {
                    if (Math.abs(dx)==radius || Math.abs(dz)==radius)
                        world.setBlockState(p.add(dx,y,dz), roof, 2);
                }
            }
            for (int dx=-2; dx<=2; dx++) for (int dz=-2; dz<=2; dz++)
                world.setBlockState(p.add(dx,10,dz), roof, 2);
            world.setBlockState(p.add(0,11,0), accent, 2);

            // Grand front staircase, porch, lanterns and interior storage.
            for (int z=-9; z<=-6; z++) for (int dx=-2; dx<=2; dx++)
                world.setBlockState(p.add(dx,0,z), accent, 2);
            for (int dx=-2; dx<=2; dx++) {
                world.setBlockState(p.add(dx,1,-7), stair, 2);
                world.setBlockState(p.add(dx,1,-6), slab, 2);
            }
            world.setBlockState(p.add(-3,3,-5), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(3,3,-5), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(3,1,3), Blocks.CHEST.getDefaultState(), 2);
            if (world.getTileEntity(p.add(3,1,3)) instanceof TileEntityChest)
                fillChest((TileEntityChest)world.getTileEntity(p.add(3,1,3)), random);
            world.setBlockState(p.add(0,2,0), Blocks.TORCH.getDefaultState(), 2);
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

        private void fillChest(TileEntityChest chest, Random random) {
            put(chest, new ItemStack(Items.IRON_INGOT, 2 + random.nextInt(5)), random);
            put(chest, new ItemStack(Items.GOLD_INGOT, 1 + random.nextInt(3)), random);
            put(chest, new ItemStack(Items.ENDER_PEARL, 1 + random.nextInt(2)), random);
            put(chest, new ItemStack(Items.EXPERIENCE_BOTTLE, 2 + random.nextInt(5)), random);
            put(chest, new ItemStack(Items.ARROW, 8 + random.nextInt(17)), random);

            for (String raw : ahznbLoot) {
                if (random.nextBoolean()) {
                    String[] parts = raw.trim().split("@");
                    ResourceLocation id = new ResourceLocation(parts[0]);
                    Item item = Item.REGISTRY.getObject(id);
                    if (item != null && item != Items.AIR) {
                        int meta = 0;
                        if (parts.length > 1) {
                            try { meta = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
                        }
                        put(chest, new ItemStack(item, 1, meta), random);
                    }
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
