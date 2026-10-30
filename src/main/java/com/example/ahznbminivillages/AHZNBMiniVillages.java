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
            for (int dx = -16; dx <= 16; dx += 4) {
                for (int dz = -16; dz <= 16; dz += 4) {
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

            int[][] homes = {
                {-10,-10}, {-3,-12}, {5,-12}, {12,-7}, {12,2},
                {8,10}, {0,12}, {-9,10}, {-13,2}, {-12,-6}
            };
            for (int[] home : homes) {
                buildNinjaHouse(world, p.add(home[0], 0, home[1]), wall, roof, accent, random);
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
            // Detailed 5x5 ninja house: raised foundation, framed walls, windows and a layered roof.
            IBlockState glass = Blocks.GLASS_PANE.getDefaultState();
            IBlockState fence = Blocks.OAK_FENCE.getDefaultState();
            IBlockState slab = Blocks.WOODEN_SLAB.getDefaultState();
            IBlockState stair = Blocks.OAK_STAIRS.getDefaultState();

            // Foundation and floor.
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    world.setBlockState(p.add(dx, -1, dz), accent, 2);
                    world.setBlockState(p.add(dx, 0, dz), wall, 2);
                }
            }

            // Three-block-high walls with a centered front entrance.
            for (int dy = 1; dy <= 3; dy++) {
                for (int i = -2; i <= 2; i++) {
                    world.setBlockState(p.add(i, dy, -2), wall, 2);
                    world.setBlockState(p.add(i, dy, 2), wall, 2);
                    world.setBlockState(p.add(-2, dy, i), wall, 2);
                    world.setBlockState(p.add(2, dy, i), wall, 2);
                }
            }
            // Open doorway and glass windows on all sides.
            for (int dy = 1; dy <= 2; dy++) world.setBlockToAir(p.add(0, dy, -2));
            world.setBlockState(p.add(-1, 2, -2), glass, 2);
            world.setBlockState(p.add(1, 2, -2), glass, 2);
            world.setBlockState(p.add(-2, 2, 0), glass, 2);
            world.setBlockState(p.add(2, 2, 0), glass, 2);
            world.setBlockState(p.add(0, 2, 2), glass, 2);

            // Corner pillars and decorative trim.
            for (int dx : new int[]{-2, 2}) for (int dz : new int[]{-2, 2}) {
                for (int dy = 1; dy <= 3; dy++) world.setBlockState(p.add(dx, dy, dz), accent, 2);
            }
            for (int dx = -2; dx <= 2; dx++) {
                world.setBlockState(p.add(dx, 3, -2), accent, 2);
                world.setBlockState(p.add(dx, 3, 2), accent, 2);
            }

            // Layered, overhanging roof silhouette.
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2)
                    world.setBlockState(p.add(dx, 4, dz), roof, 2);
            }
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
                world.setBlockState(p.add(dx, 5, dz), roof, 2);
            world.setBlockState(p.add(0, 6, 0), accent, 2);

            // Interior lighting, chest and simple entry porch.
            world.setBlockState(p.add(0, 2, 0), Blocks.TORCH.getDefaultState(), 2);
            world.setBlockState(p.add(1, 1, 1), Blocks.CHEST.getDefaultState(), 2);
            if (world.getTileEntity(p.add(1, 1, 1)) instanceof TileEntityChest) {
                fillChest((TileEntityChest) world.getTileEntity(p.add(1, 1, 1)), random);
            }
            for (int dx = -1; dx <= 1; dx++) {
                world.setBlockState(p.add(dx, 0, -3), slab, 2);
            }
            world.setBlockState(p.add(-2, 1, -3), fence, 2);
            world.setBlockState(p.add(2, 1, -3), fence, 2);
            world.setBlockState(p.add(0, 1, -3), stair, 2);
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
