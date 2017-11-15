package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;

import java.util.List;
import java.util.logging.Logger;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Folder;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestFolderReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestItemReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.VariableStorage;

/**
 * Runs tests contained in a folder.
 *
 */
public class TestFolderRunner {
	private static final Logger LOGGER = Logger.getLogger(TestFolderRunner.class.getName());
	private String url;
	private Folder folder;
	private String baseUrl;
	private VariableStorage storage;
	
	/**
	 * Constructor
	 * @param baseUrl Base URL of the endpoint to be tested
	 * @param folder Folder config instance
	 */
	public TestFolderRunner(String baseUrl, Folder folder) {
		this.baseUrl = baseUrl.replaceAll("/$", ""); //Make sure there is no trailing slash;
		this.folder = folder;
		this.storage = new VariableStorage(this.baseUrl);
	}
	
	/**
	 * Runs the tests specified in the Folder config
	 * @return Test report.
	 */
	public TestFolderReport runTests() {
		TestFolderReport tcr = new TestFolderReport(url);
		tcr.setName(this.folder.getName());
		tcr.setDescription(this.folder.getDescription());
		List<Item> itemList = this.folder.getItem();
		for (int i = 0; i < itemList.size(); i++) {
			Item item = itemList.get(i);
			TestItemRunner tir = new TestItemRunner(item, storage);
			TestItemReport tiReport = tir.runTests();
			tcr.addTestReport(tiReport);
		}
		tcr.setVariables(storage);
		return tcr;
	}
}
