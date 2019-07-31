package betterquesting.network;

import betterquesting.core.BetterQuesting;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketAssembly {
	public static final PacketAssembly INSTANCE = new PacketAssembly();
	private final ConcurrentHashMap<UUID, byte[]> buffer = new ConcurrentHashMap<>();
	private byte[] serverBuf = null;
	private int id = 0;

	public ArrayList<NBTTagCompound> splitPacket(NBTTagCompound tags) {
		ArrayList<NBTTagCompound> pkts = new ArrayList<>();
		try {
			byte[] data = CompressedStreamTools.compress(tags);
			int req = MathHelper.ceiling_float_int(data.length / 30000F);
			for(int p = 0; p < req; p++) {
				int idx = p * 30000;
				int s = Math.min(data.length - idx, 30000);
				NBTTagCompound container = new NBTTagCompound();
				byte[] part = new byte[s];
				System.arraycopy(data, idx, part, 0, s);
				container.setInteger("size", data.length);
				container.setInteger("index", idx);
				container.setBoolean("end", p == req - 1);
				container.setTag("data", new NBTTagByteArray(part));
				pkts.add(container);
			}
		} catch(Exception e) {
			BetterQuesting.logger.info("Unable to build packet", e);
		}
		id = (id + 1) % 100;
		return pkts;
	}

	public NBTTagCompound assemblePacket(UUID owner, NBTTagCompound tags) {
		int size = tags.getInteger("size"),
			index = tags.getInteger("index");
		boolean end = tags.getBoolean("end");
		byte[] data = tags.getByteArray("data"),
			tmp = getBuffer(owner);
		if(tmp == null || tmp.length != size) {
			tmp = new byte[size];
			setBuffer(owner, tmp);
		}
		for(int i = 0; i < data.length && index + i < size; i++) {
			tmp[index + i] = data[i];
		}
		if(end) {
			clearBuffer(owner);
			try {
				return CompressedStreamTools.decompress(tmp, NBTSizeTracker.INFINITE);
			} catch(Exception e) {
				BetterQuesting.logger.info("Unable to assemble packet", e);
			}
		}
		return null;
	}

	public byte[] getBuffer(UUID owner) {
		if(owner == null) {
			return serverBuf;
		} else {
			return buffer.get(owner);
		}
	}

	public void setBuffer(UUID owner, byte[] value) {
		if(owner == null) {
			serverBuf = value;
		} else {
			buffer.put(owner, value);
		}
	}

	public void clearBuffer(UUID owner) {
		if(owner == null) {
			serverBuf = null;
		} else {
			buffer.remove(owner);
		}
	}
}