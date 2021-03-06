package exnihiloadscensio.blocks;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import exnihiloadscensio.barrel.modes.block.BarrelModeBlock;
import exnihiloadscensio.barrel.modes.compost.BarrelModeCompost;
import exnihiloadscensio.barrel.modes.fluid.BarrelModeFluid;
import exnihiloadscensio.barrel.modes.transform.BarrelModeFluidTransform;
import exnihiloadscensio.compatibility.theoneprobe.ITOPInfoProvider;
import exnihiloadscensio.config.Config;
import exnihiloadscensio.tiles.TileBarrel;
import exnihiloadscensio.util.Util;
import lombok.Getter;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoAccessor;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public class BlockBarrel extends BlockBase implements ITileEntityProvider, ITOPInfoProvider {

	private AxisAlignedBB boundingBox = new AxisAlignedBB(0.0625f, 0, 0.0625f, 0.9375f, 1f, 0.9375f);
	@Getter
	private int tier;
	
	public BlockBarrel(int tier, Material material)
	{
		super(material, "blockBarrel" + tier);
        this.tier = tier;
		this.setHardness(2.0f);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TileBarrel) {
			TileBarrel barrel = (TileBarrel) te;
			
			if (barrel.getMode() != null && barrel.getMode().getName().equals("block")) {
				ItemStack stack = barrel.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
				if (stack != null)
					Util.dropItemInWorld(te, null, stack, 0);
			}
		}
		
		super.breakBlock(world, pos, state);
	}
	@SuppressWarnings("deprecation")
    @Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
	{
	    TileEntity te = world.getTileEntity(pos);
	    
	    if(te != null && te instanceof TileBarrel)
	    {
	    	TileBarrel tile = (TileBarrel) te;
	        if(tile.getMode() instanceof BarrelModeBlock)
	        {
	            BarrelModeBlock mode = (BarrelModeBlock) tile.getMode();
	            
	            if(mode.getBlock() != null)
	            {
	                return Block.getBlockFromItem(mode.getBlock().getItem()).getStateFromMeta(mode.getBlock().getMeta()).getLightValue();
	            }
	        }
            else if(tile.getMode() instanceof BarrelModeFluid)
            {
                BarrelModeFluid mode = (BarrelModeFluid) tile.getMode();
                
                if(mode.getFluidHandler(tile).getFluidAmount() > 0)
                {
                    return Util.getLightValue(mode.getFluidHandler(tile).getFluid());
                }
            }
            else if(Config.enableBarrelTransformLighting)
            {
                if(tile.getMode() instanceof BarrelModeCompost)
                {
                    BarrelModeCompost mode = (BarrelModeCompost) tile.getMode();
                    
                    if(mode.getCompostState() != null)
                    {
                        int value = mode.getCompostState().getLightValue() / 2;
                        
                        return Math.round(Util.weightedAverage(value / 2, value, mode.getProgress()));
                    }
                }
    	        else if(tile.getMode() instanceof BarrelModeFluidTransform)
    	        {
    	            BarrelModeFluidTransform mode = (BarrelModeFluidTransform) tile.getMode();
    	            
    	            int inputValue = Util.getLightValue(mode.getInputStack());
    	            int outputValue = Util.getLightValue(mode.getOutputStack());
    	            
    	            return Math.round(Util.weightedAverage(outputValue, inputValue, mode.getProgress()));
    	        }
            }
	    }
	    
	    return super.getLightValue(state, world, pos);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		return ((TileBarrel) world.getTileEntity(pos)).onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
	}
	
	@Override
	public boolean isFullyOpaque(IBlockState state)
	{
		return false;
	}
	
	@Override
	 public boolean isFullBlock(IBlockState state)
    {
        return false;
    }
	
	@Override
	public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		return new TileBarrel(this);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		return boundingBox;
	}
	
    @Override
    public boolean isBlockSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return false;
    }
    
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
			IBlockState blockState, IProbeHitData data) {
		TileBarrel barrel = (TileBarrel) world.getTileEntity(data.getPos());
		if (barrel == null)
			return;
		if (mode == ProbeMode.EXTENDED)
			probeInfo.text(TextFormatting.GREEN + "Mode: "+StringUtils.capitalize(barrel.getMode().getName()));
		
		List<String> tooltips = barrel.getMode().getWailaTooltip(barrel, new ArrayList<String>());
		for (String tooltip : tooltips) {
			probeInfo.text( tooltip);
		}
	}

}
