import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;

import javax.swing.SwingWorker;

public class GenerationWorker extends SwingWorker<Void, String> {
	private static final int UNLOCK_BIN_SIZE = 1024;
	private static final int UINT32_T_SIZE = 4;
	private static final int SIGNATURE_SIZE = 512;
	private static final int KEY_SIZE = 256;
	private static final int EXTRA_SIZE = 492;
	private static final long SECURITY_UNLOCK_MAGIC1 = 2377586078L; // 0x8DB7159E
	private static final long SECURITY_UNLOCK_MAGIC2 = 763286379L; // 0x2D7ED36B
	private static final long SECURITY_UNLOCK_VERSION = 1L;
	private static final int IMEI_SIZE = 32;
	private static final int DEVICE_ID_SIZE = 96;
	// RSA_UNLOCK_02
	private static final RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(
			"18536265221834400955526124823946945144241534366405270883862606828214326557303158761374427696439760867810300046710668389940627901357786930619155280232713255180467267693281615312585736047834931276426122242381388755141769507773314618374615964530031495500324126445550145922318729183762394336526893965841523887301431217744349619177044755418369600023019646764547203434859153096499560007159303235140562773302106895748271986503337696246115511449909141742149128001718847058167094531480513164043443149146227140700654562659385941009377485565173992175722386093166833729231966326215327030617445434971297334403421561820089441204503"),
			new BigInteger("65537"));
	private final String imei;
	private final String deviceId;
	private final File file;
	private final boolean saveProgresses;
	private int attempts = 0;
	private final byte[] firstSignature;
	private final byte[] secondSignature;

	public GenerationWorker(String imei, String deviceId, String outputFile, boolean saveProgresses) throws IOException {
		System.out.println(new java.util.Date() + " - Start");
		System.out.println("Imei: " + imei);
		System.out.println("DeviceId: " + deviceId);
		System.out.println("OutputFile: " + outputFile);

		this.imei = imei;
		this.deviceId = deviceId;
		this.file = new File(outputFile);
		this.saveProgresses = saveProgresses;
		this.firstSignature = new byte[KEY_SIZE];
		this.secondSignature = new byte[KEY_SIZE];

		byte[] fileContent = Files.readAllBytes(this.file.toPath());
		if (fileContent.length == UNLOCK_BIN_SIZE) { // FILE EXISTS AND LENGHT MATCHES
			System.out.println(new java.util.Date() + " - File exists and length matches!");

			int offset = 0;

			byte[] magic1 = new byte[UINT32_T_SIZE];
			System.arraycopy(fileContent, offset, magic1, 0, UINT32_T_SIZE);

			offset += UINT32_T_SIZE;

			byte[] magic2 = new byte[UINT32_T_SIZE];
			System.arraycopy(fileContent, offset, magic2, 0, UINT32_T_SIZE);

			offset += UINT32_T_SIZE;

			byte[] version = new byte[UINT32_T_SIZE];
			System.arraycopy(fileContent, offset, version, 0, UINT32_T_SIZE);

			offset += UINT32_T_SIZE;

			byte[] hash_type = new byte[UINT32_T_SIZE];
			System.arraycopy(fileContent, offset, hash_type, 0, UINT32_T_SIZE);

			offset += UINT32_T_SIZE;

			byte[] key_size = new byte[UINT32_T_SIZE];
			System.arraycopy(fileContent, offset, key_size, 0, UINT32_T_SIZE);

			offset += UINT32_T_SIZE;

			if (deserialize_uint32(magic1) == SECURITY_UNLOCK_MAGIC1
					&& deserialize_uint32(magic2) == SECURITY_UNLOCK_MAGIC2
					&& deserialize_uint32(version) == SECURITY_UNLOCK_VERSION) { // MAGIC NUMBERS FOUND AND MATCHES
				System.out.println(new java.util.Date() + " - Magic numbers found and correct!");

				byte[] signature = new byte[SIGNATURE_SIZE];
				System.arraycopy(fileContent, offset, signature, 0, SIGNATURE_SIZE);

				System.arraycopy(signature, 0, firstSignature, 0, KEY_SIZE);

				offset += SIGNATURE_SIZE;

				byte[] extra = new byte[EXTRA_SIZE];
				System.arraycopy(fileContent, offset, extra, 0, EXTRA_SIZE);

				System.arraycopy(signature, KEY_SIZE + 12, secondSignature, 0, KEY_SIZE - 12);
				System.arraycopy(extra, 0, secondSignature, KEY_SIZE - 12, 12);
			} else {
				System.out.println(new java.util.Date() + " - Magic numbers not found");
			}
		} else {
			System.out.println(new java.util.Date() + " - Invalid file! Generating a new one...");
		}
	}

	@Override
	protected void done() {
		try {
			byte[] fileContent = new byte[UNLOCK_BIN_SIZE];
			int offset = 0;

			System.arraycopy(serialize_uint32(SECURITY_UNLOCK_MAGIC1), 0, fileContent, offset, UINT32_T_SIZE);

			offset += UINT32_T_SIZE;

			System.arraycopy(serialize_uint32(SECURITY_UNLOCK_MAGIC2), 0, fileContent, offset, UINT32_T_SIZE);

			offset += UINT32_T_SIZE;

			System.arraycopy(serialize_uint32(SECURITY_UNLOCK_VERSION), 0, fileContent, offset, UINT32_T_SIZE);

			offset += UINT32_T_SIZE;

			System.arraycopy(serialize_uint32(2L), 0, fileContent, offset, UINT32_T_SIZE); // hash_type

			offset += UINT32_T_SIZE;

			System.arraycopy(serialize_uint32(KEY_SIZE), 0, fileContent, offset, UINT32_T_SIZE);

			offset += UINT32_T_SIZE;

			System.arraycopy(firstSignature, 0, fileContent, offset, KEY_SIZE);

			offset += KEY_SIZE + 12;

			System.arraycopy(secondSignature, 0, fileContent, offset, KEY_SIZE);

			Files.write(this.file.toPath(), fileContent);
			System.out.println(new java.util.Date() + " - Writing file to disk");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Void doInBackground() throws Exception {
		final KeyFactory f = KeyFactory.getInstance("RSA");
		final PublicKey publicKey = f.generatePublic(spec);
		final byte[] input = getInput(this.imei, this.deviceId);

		System.out.println(new java.util.Date() + " - Start attacking first signature");
		bruteForceFirstSignature(publicKey, input);

		System.out.println(new java.util.Date() + " - Start attacking second signature");
		bruteForceSecondSignature(publicKey, input);
		return null;
	}

	private void bruteForceFirstSignature(PublicKey publicKey, byte[] input) throws Exception {
		final Signature firstSignatureVerify = Signature.getInstance("NonewithRSA");
		firstSignatureVerify.initVerify(publicKey);
		firstSignatureVerify.update(MessageDigest.getInstance("SHA-256").digest(input));

		while (true) {
			if (isCancelled()) {
				System.out.println(new java.util.Date() + " - User cancelled");
				return;
			}
			if (firstSignatureVerify.verify(this.firstSignature)) {
				System.out.println(new java.util.Date() + " - First signature verified");
				return;
			}
			if (this.saveProgresses && ++attempts % 10000001 == 0) {
				this.done();
			}
			increment(this.firstSignature);
		}
	}

	private void bruteForceSecondSignature(PublicKey publicKey, byte[] input) throws Exception {
		final Signature secondSignatureVerify = Signature.getInstance("SHA256withRSA");
		secondSignatureVerify.initVerify(publicKey);
		secondSignatureVerify.update(input);

		while (true) {
			if (isCancelled()) {
				System.out.println(new java.util.Date() + " - User cancelled");
				return;
			}
			if (secondSignatureVerify.verify(secondSignature)) {
				System.out.println(new java.util.Date() + " - Second signature verified");
				return;
			}
			if (this.saveProgresses && ++attempts % 10000001 == 0) {
				this.done();
			}
			increment(secondSignature);
		}
	}

	private static void increment(byte[] arr) {
		int i = arr.length - 1;
		while (i >= 0 && arr[i] == (byte) 0xFF) {
			arr[i] = 0;
			i--;
		}
		if (i >= 0) {
			arr[i]++;
		}
	}

	private static byte[] getInput(String imei, String deviceId) {
		byte[] input = new byte[DEVICE_ID_SIZE + IMEI_SIZE];
		System.arraycopy(deviceId.getBytes(), 0, input, 0, deviceId.length());
		System.arraycopy(imei.getBytes(), 0, input, DEVICE_ID_SIZE, imei.length());
		return input;
	}

	private static byte[] serialize_uint32(long l) {
		byte[] b = new byte[4];
		b[0] = (byte) l;
		b[1] = (byte) (l >> 8);
		b[2] = (byte) (l >> 16);
		b[3] = (byte) (l >> 24);
		return b;
	}

	private static long deserialize_uint32(byte[] b) {
		long l = (long) b[0] & 0xFF;
		l += ((long) b[1] & 0xFF) << 8;
		l += ((long) b[2] & 0xFF) << 16;
		l += ((long) b[3] & 0xFF) << 24;
		return l;
	}
}
