package com.tibco.bw.studio.maven.wizard;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.tibco.bw.studio.maven.helpers.ManifestParser;
import com.tibco.bw.studio.maven.helpers.ModuleHelper;
import com.tibco.bw.studio.maven.modules.model.BWApplication;
import com.tibco.bw.studio.maven.modules.model.BWDeploymentInfo;
import com.tibco.bw.studio.maven.modules.model.BWModule;
import com.tibco.bw.studio.maven.modules.model.BWModuleType;
import com.tibco.bw.studio.maven.modules.model.BWProject;

public class WizardPageEnterprise extends WizardPage {
	private Composite container;
	private BWProject project;
	private String bwEdition;
	private Combo agentAuth;
	private Text agentHost;
	private Text agentPort;
	private Text agentUser;
	private Text agentPass;
	private Button agentSSL;
	private Text trustPath;
	private Text trustPass;
	private Text keyPath;
	private Text keyPass;
	private Text domain;
	private Text domainDesc;
	private Text appspace;	
	private Text appspaceDesc;
	private Text appNode;
	private Text appNodeDesc;
	private Button redeploy;
	private Button backup;
	private Text backupLocation;
	private Text externalProfileLoc;
	private Text httpPort;
	private Text osgiPort;
	private Combo profile;
	private BWModule appModule;
	private BWDeploymentInfo info;
	private static final String BASIC_AUTH = "BASIC";
	private static final String DIGEST_AUTH = "DIGEST";
	private int index=0;
	private int textHeight = 18;
	private Table tableAppNodeConfig;
	private Button restartAppNode;
	
	protected WizardPageEnterprise(String pageName, BWProject project) {
		super(pageName);
		this.project = project;		 
		setTitle("Deployment Details for TIBCO BusinessWorks(TM) Application");
		setDescription("Please enter the Deployment details to Deploy the EAR file to BWAgent.");	
	}

	public boolean validate() {
		StringBuffer errorMessage = new StringBuffer();
		boolean isValidHost = !agentHost.getText().isEmpty();
		if(!isValidHost) {
			errorMessage.append("[Agent Host value is required]");
		}
		boolean isValidPort = false;
		try {
			if(agentPort.getText().isEmpty()) {
				errorMessage.append("[Agent Port value is required]");
			} else if(Integer.parseInt(agentPort.getText()) < 0) {
				errorMessage.append("[Agent Port value must be an Integer]");
			} else {
				isValidPort = true;
			}
		} catch(Exception e) {
			errorMessage.append("[Agent Port value must be an Integer]");
		}

		boolean isValidDomain = !domain.getText().isEmpty();
		if(!isValidDomain) {
			errorMessage.append("[Domain value is required]");
		}

		boolean isValidAppSpace = !appspace.getText().isEmpty(); 
		if(!isValidAppSpace) {
			errorMessage.append("[AppSpace value is required]");
		}

		boolean isValidAppNode = !appNode.getText().isEmpty();
		if(!isValidAppNode) {
			errorMessage.append("[AppNode value is required]");
		}

		boolean isValidHTTPPort = false;
		try {
			if(httpPort.getText().isEmpty()) {
				errorMessage.append("[HTTP Port value is required]");
			} else if(Integer.parseInt(httpPort.getText()) < 0) {
				errorMessage.append("[HTTP Port value must be an Integer]");
			} else {
				isValidHTTPPort = true;
			}
		} catch(Exception e) {
			errorMessage.append("[HTTP Port value must be an Integer]");
		}

		boolean isValidOSGi = false;
		try {
			if(osgiPort.getText().isEmpty()) {
				isValidOSGi = true;
			} else if(Integer.parseInt(osgiPort.getText()) < 0) {
				isValidOSGi = false;
				errorMessage.append("[OSGi Port value must be an Integer]");
			} else {
				isValidOSGi = true;
			}
		} catch(Exception e) {
			errorMessage.append("[OSGi Port value must be an Integer]");
		}

		boolean isValidBackupLoc = true;
		if(backup.getSelection() && backupLocation.getText().isEmpty()) {
			isValidBackupLoc = false;
			errorMessage.append("[Backup Location value is required]");
		}
		
		boolean isValidexternalProfileLoc = true;
		if(profile.getItem(index)=="other" && externalProfileLoc.getText().isEmpty()) {
			isValidexternalProfileLoc = false;
			errorMessage.append("[external Profile Location value is required]");
		}

		boolean isValidCredential = true;
		if(agentAuth.getText() != null && (BASIC_AUTH.equalsIgnoreCase(agentAuth.getText()) || DIGEST_AUTH.equalsIgnoreCase(agentAuth.getText()))) {
			if(agentUser.getText() == null || agentUser.getText().isEmpty()) {
				isValidCredential = false;
				errorMessage.append("[Agent Username value is required]");
			}
			if(agentPass.getText() == null || agentPass.getText().isEmpty()) {
				isValidCredential = false;
				errorMessage.append("[Agent Password value is required]");
			}
		}

		boolean isValidSSL = true;
		if(agentSSL.getSelection()) {
			if(trustPath.getText() == null || trustPath.getText().isEmpty()) {
				isValidSSL = false;
				errorMessage.append("[Truststore Path value is required]");
			}
			if(trustPass.getText() == null || trustPass.getText().isEmpty()) {
				isValidSSL = false;
				errorMessage.append("[Truststore Password value is required]");
			}
		}

		if(!errorMessage.toString().isEmpty()) {
			setErrorMessage(errorMessage.toString());
			return false;
		}
		if(isValidHost && isValidPort && isValidDomain && isValidAppSpace && isValidAppNode && isValidHTTPPort && isValidOSGi && isValidBackupLoc && isValidCredential && isValidSSL && isValidexternalProfileLoc) {
			return true;
		}
		return false;
	}

	@Override
	public void createControl(Composite parent) {
		
	
		
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		container.setLayout(layout);
		layout.numColumns = 4;
		appModule = ModuleHelper.getAppModule(project.getModules());
		info = ((BWApplication)ModuleHelper.getApplication(project.getModules())).getDeploymentInfo();
		bwEdition = "bw6";
		try {
			Map<String, String> manifest = ManifestParser.parseManifest(project.getModules().get(0).getProject());
			if(manifest.containsKey("TIBCO-BW-Edition") && manifest.get("TIBCO-BW-Edition").equals("bwcf")) {
				bwEdition = "bwcf";
			} else {
				bwEdition = "bw6";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		addNotes();
		addSeperator(parent);
		addDeploymentFields(parent);
		setControl(container);
		setPageComplete(true);
	}

	private void addNotes() {
		Group noteGroup = new Group(container, SWT.SHADOW_ETCHED_IN);
		noteGroup.setText("Note : ");
		noteGroup.setLayout(new GridLayout(1, false));
		GridData noteData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		noteData.horizontalSpan = 4;
		noteGroup.setLayoutData(noteData);
		Label label = new Label(noteGroup, SWT.NONE);
		label.setText("- The EAR file will be deployed to the Agent provided below during the Maven \"install\" lifecycle phase.\r\n"
				+ "- If the Domain, Appspace and AppNode do not exist then they will be created.\r\n"
				+ "- The Application within EAR file will be started on deployment");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private void addSeperator(Composite parent) {
		Label horizontalLine = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DASH);
		horizontalLine.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 4, 1));
		horizontalLine.setFont(parent.getFont());
	}

	public BWProject getUpdatedProject() {
		for (BWModule module : project.getModules()) {
			if(bwEdition.equals("bw6") && module.getType() == BWModuleType.Application) {
				BWDeploymentInfo info = ((BWApplication)module).getDeploymentInfo();
				info.setAgentHost(agentHost.getText());
				info.setAgentPort(agentPort.getText());
				info.setAgentAuth(agentAuth.getText());
				info.setAgentUsername(agentUser.getText());
				info.setAgentPassword(agentPass.getText());
				info.setAgentSSL(agentSSL.getSelection());
				info.setTrustPath(trustPath.getText());
				info.setTrustPassword(trustPass.getText());
				info.setKeyPath(keyPath.getText());
				info.setKeyPassword(keyPass.getText());
				info.setDomain(domain.getText());
				info.setDomainDesc(domainDesc.getText());
				info.setAppspace(appspace.getText());
				info.setAppspaceDesc(appspaceDesc.getText());
				info.setAppNode(appNode.getText());
				info.setAppNodeDesc(appNodeDesc.getText());
				info.setHttpPort(httpPort.getText());
				info.setOsgiPort(osgiPort.getText());
				info.setProfile(profile.getText());
				info.setProfiles(getProfiles());
				info.setRedeploy(redeploy.getSelection());
				info.setBackup(backup.getSelection());
				info.setBackupLocation(backupLocation.getText());
				info.setexternalProfile(info.isexternalProfile());
				info.setexternalProfileLoc(externalProfileLoc.getText());
				for(TableItem item : tableAppNodeConfig.getItems()){
					info.getAppNodeConfig().put(item.getText(0), item.getText(1));
				}
				info.setRestartAppNode(restartAppNode.getSelection());
			}
			module.setOverridePOM(true);
		}
		return project;
	}

	private void addDeploymentFields(Composite parent) {
		addAgentInfo();
		addSeperator(parent);
		addDomain();
		addAppSpace();
		addSeperator(parent);
		addAppNode();
		addAppNodeConfig();
		addSeperator(parent);
		addProfile();
		addSeperator(parent);
	}

	private void addAppNodeConfig(){
		Label appNodeConfigLabel = new Label(container, SWT.NONE);
		appNodeConfigLabel.setText("AppNode Configuration");
		
		tableAppNodeConfig = new Table (container, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		tableAppNodeConfig.setLinesVisible(true);
		tableAppNodeConfig.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
		data.heightHint = 80;
		data.horizontalSpan = 2;
		tableAppNodeConfig.setLayoutData(data);
		String[] titles = {"Config Name            ", "Value                                    "};
		for (String title : titles) {
			TableColumn column = new TableColumn (tableAppNodeConfig, SWT.NONE);
			column.setText (title);
			column.setResizable(true);
			//column.setWidth(tableAppNodeConfig.getBounds().width / 2);
		}
		
		if(info.getAppNodeConfig()!= null && !info.getAppNodeConfig().isEmpty())
		{
		for(String key : info.getAppNodeConfig().keySet())
		{
			TableItem item = new TableItem (tableAppNodeConfig, SWT.NONE);
			item.setText (0, (key != null ? key.trim() : ""));
			item.setText (1, (info.getAppNodeConfig().get(key) != null ? info.getAppNodeConfig().get(key).trim() : ""));
		}
		} else {
			TableItem item = new TableItem (tableAppNodeConfig, SWT.NONE);
			item.setText (0, "bw.rest.docApi.port");
			item.setText (1, "7777");
		}
	
		for (int i=0; i<titles.length; i++) {
			tableAppNodeConfig.getColumn (i).pack ();
		}
		
		final TableEditor editor = new TableEditor(tableAppNodeConfig);
		//The editor must have the same size as the cell and must
		//not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
		// editing the second column
		final int EDITABLECOLUMN = 1;

		tableAppNodeConfig.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// Clean up any previous editor control
				Control oldEditor = editor.getEditor();
				if (oldEditor != null)
					oldEditor.dispose();

				TableItem item = (tableAppNodeConfig.getSelection().length > 0 ? tableAppNodeConfig.getSelection()[0] : null);
				if(item == null)
					return;
				
				int column = EDITABLECOLUMN;
				Point pt = new Point (e.x, e.y);
				for(int i=0;i<tableAppNodeConfig.getColumnCount();i++)
				{
					Rectangle rect = item.getBounds (i);
					if (rect.contains (pt)) {
						column = i;
					}
				}
				// The control that will be the editor must be a child of the Table
				final Text newEditor = new Text(tableAppNodeConfig, SWT.NONE);
				newEditor.setText(item.getText(column));
				newEditor.setData(column);
				newEditor.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent arg0) {
						Text text = (Text) editor.getEditor();
						int column = (int) newEditor.getData();
						editor.getItem().setText(column, text.getText());
					}
				});
				newEditor.selectAll();
				newEditor.setFocus();
				editor.setEditor(newEditor, item, column);

				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
					
		Composite buttonComp = new Composite(container, SWT.NONE | SWT.TOP);
		GridData gd = new GridData(150, 80);
		buttonComp.setLayoutData(gd);
		buttonComp.setLayout(new GridLayout(1, false));
		Button addVar = new Button(buttonComp, SWT.PUSH);
		Image imageAdd = new Image(buttonComp.getDisplay(),  getClass().getClassLoader().getResourceAsStream("icons/add_16_16.png"));
		addVar.setImage(imageAdd);
		//addVar.setText("Add");
		addVar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem item = new TableItem (tableAppNodeConfig, SWT.NONE);
				item.setText (0, "VariableName");
				item.setText (1, "value");
			}
		});
		
		Button removeVar = new Button(buttonComp, SWT.PUSH);
		Image imageRemove = new Image(buttonComp.getDisplay(),  getClass().getClassLoader().getResourceAsStream("icons/remove_16_16.png"));
		removeVar.setImage(imageRemove);
		//removeVar.setText("Remove");
		removeVar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableAppNodeConfig.remove(tableAppNodeConfig.getSelectionIndices());
				for(Control ctrl : tableAppNodeConfig.getChildren())
					if(ctrl.getClass() == Text.class)
						ctrl.dispose();
				tableAppNodeConfig.redraw();
			}
		});
		
		Label restartAppNodeLabel = new Label(container, SWT.NONE);
		restartAppNodeLabel.setText("Restart AppNode");
		restartAppNode = new Button(container, SWT.CHECK);
		restartAppNode.setSelection(info.isRestartAppNode());

	}
	
	private void addAgentInfo() {
		Label agentLabel = new Label(container, SWT.NONE);
		agentLabel.setText("Agent Host");
		agentHost = new Text(container, SWT.BORDER | SWT.SINGLE);
		agentHost.setText(info.getAgentHost());
		GridData agentData = new GridData(150, textHeight);
		agentHost.setLayoutData(agentData);

		Label agentPortLabel = new Label(container, SWT.NONE);
		agentPortLabel.setText("Agent Port");
		agentPort = new Text(container, SWT.BORDER | SWT.SINGLE);
		agentPort.setText(info.getAgentPort());
		agentPort.setLayoutData(new GridData(100, textHeight));

		Label agentAuthLabel = new Label(container, SWT.NONE);
		agentAuthLabel.setText("Agent Authentication");
		agentAuth = new Combo(container, SWT.BORDER | SWT.SINGLE);
		agentAuth.add("");
		agentAuth.add(BASIC_AUTH);
		agentAuth.add(DIGEST_AUTH);
		agentAuth.setText(info.getAgentAuth());
		GridData agentAuthData = new GridData(135, textHeight);
		agentAuthData.horizontalSpan = 3;
		agentAuth.setLayoutData(agentAuthData);

		Label agentUserLabel = new Label(container, SWT.NONE);
		agentUserLabel.setText("Agent Username");
		agentUser = new Text(container, SWT.BORDER | SWT.SINGLE);
		agentUser.setText(info.getAgentUsername());
		agentUser.setLayoutData(agentData);

		Label agentPassLabel = new Label(container, SWT.NONE);
		agentPassLabel.setText("Agent Password");
		agentPass = new Text(container, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		agentPass.setText(info.getAgentPassword());
		agentPass.setLayoutData(agentData);

		if(info.getAgentAuth() == null || info.getAgentAuth().isEmpty()) {
			agentUser.setText("");
			agentUser.setEnabled(false);
			agentPass.setText("");
			agentPass.setEnabled(false);
		}
		
		agentAuth.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(agentAuth.getSelectionIndex() > 0) {
					agentUser.setEnabled(true);
					agentPass.setEnabled(true);
				} else {
					agentUser.setText("");
					agentUser.setEnabled(false);
					agentPass.setText("");
					agentPass.setEnabled(false);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		Label agentSslLabel = new Label(container, SWT.NONE);
		agentSslLabel.setText("SSL Connection");
		agentSSL = new Button(container, SWT.CHECK);
		agentSSL.setSelection(info.isAgentSSL());
		GridData sslData = new GridData(200, textHeight);
		sslData.horizontalSpan = 3;
		agentSSL.setLayoutData(sslData);

		GridData storeData = new GridData(200, textHeight);
		
		Label trustPathLabel = new Label(container, SWT.NONE);
		trustPathLabel.setText("Truststore Path");
		trustPath = new Text(container, SWT.BORDER | SWT.SINGLE);
		trustPath.setText(info.getTrustPath());
		trustPath.setLayoutData(storeData);

		Label trustPassLabel = new Label(container, SWT.NONE);
		trustPassLabel.setText("Truststore Password");
		trustPass = new Text(container, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		trustPass.setText(info.getTrustPassword());
		trustPass.setLayoutData(agentData);

		Label keyPathLabel = new Label(container, SWT.NONE);
		keyPathLabel.setText("Keystore Path");
		keyPath = new Text(container, SWT.BORDER | SWT.SINGLE);
		keyPath.setText(info.getKeyPath());
		keyPath.setLayoutData(storeData);

		Label keyPassLabel = new Label(container, SWT.NONE);
		keyPassLabel.setText("Keystore Password");
		keyPass = new Text(container, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		keyPass.setText(info.getKeyPassword());
		keyPass.setLayoutData(agentData);

		if(!info.isAgentSSL()) {
			trustPath.setText("");
			trustPath.setEnabled(false);
			trustPass.setText("");
			trustPass.setEnabled(false);
			keyPath.setText("");
			keyPath.setEnabled(false);
			keyPass.setText("");
			keyPass.setEnabled(false);
		}

		agentSSL.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(agentSSL.getSelection()) {
					trustPath.setEnabled(true);
					trustPass.setEnabled(true);
					keyPath.setEnabled(true);
					keyPass.setEnabled(true);
				} else {
					trustPath.setText("");
					trustPath.setEnabled(false);
					trustPass.setText("");
					trustPass.setEnabled(false);
					keyPath.setText("");
					keyPath.setEnabled(false);
					keyPass.setText("");
					keyPass.setEnabled(false);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}

	private void addDomain() {
		Label domainLabel = new Label(container, SWT.NONE);
		domainLabel.setText("Domain");

		domain = new Text(container, SWT.BORDER | SWT.SINGLE);
		if(info.getDomain() != null && !info.getDomain().isEmpty()) {
			domain.setText(info.getDomain());
		} else {
			domain.setText(appModule.getArtifactId() + "-Domain");	
		}

		GridData domainData = new GridData(200, textHeight);
		domain.setLayoutData(domainData);

		Label domainDescLabel = new Label(container, SWT.NONE);
		domainDescLabel.setText("Description");

		domainDesc = new Text(container, SWT.BORDER | SWT.SINGLE);
		domainDesc.setText(info.getDomainDesc());
		GridData domainDescData = new GridData(300, textHeight);
		domainDesc.setLayoutData(domainDescData);
	}

	private void addAppSpace() {
		Label appspaceLabel = new Label(container, SWT.NONE);
		appspaceLabel.setText("AppSpace");

		appspace = new Text(container, SWT.BORDER | SWT.SINGLE);
		if(info.getAppspace() != null && !info.getAppspace().isEmpty()) {
			appspace.setText(info.getAppspace());
		} else {
			appspace.setText(appModule.getArtifactId() + "-AppSpace");	
		}

		GridData appspaceData = new GridData(200, textHeight);
		appspace.setLayoutData(appspaceData);

		Label appspaceDescLabel = new Label(container, SWT.NONE);
		appspaceDescLabel.setText("Description");

		appspaceDesc = new Text(container, SWT.BORDER | SWT.SINGLE);
		appspaceDesc.setText(info.getAppspaceDesc());
		GridData appspaceDescData = new GridData(300, textHeight);
		appspaceDesc.setLayoutData(appspaceDescData);
	}

	private void addAppNode() {
		Label appNodeLabel = new Label(container, SWT.NONE);
		appNodeLabel.setText("AppNode");

		appNode = new Text(container, SWT.BORDER | SWT.SINGLE);
		if(info.getAppNode() != null && !info.getAppNode().isEmpty()) {
			appNode.setText(info.getAppNode());
		} else {
			appNode.setText(appModule.getArtifactId() + "-AppNode");	
		}

		GridData appNodeData = new GridData(200, textHeight);
		appNode.setLayoutData(appNodeData);

		Label appnodeDescLabel = new Label(container, SWT.NONE);
		appnodeDescLabel.setText("Description");

		appNodeDesc = new Text(container, SWT.BORDER | SWT.SINGLE);
		appNodeDesc.setText(info.getAppNodeDesc());
		GridData appnodeDescData = new GridData(300, textHeight);
		appNodeDesc.setLayoutData(appnodeDescData);

		Label httpLabel = new Label(container, SWT.NONE);
		httpLabel.setText("HTTP Port");

		httpPort = new Text(container, SWT.BORDER | SWT.SINGLE);
		httpPort.setText(info.getHttpPort());
		httpPort.setLayoutData(new GridData(100, textHeight));

		Label osgiPortLabel = new Label(container, SWT.NONE);
		osgiPortLabel.setText("OSGI Port");

		osgiPort = new Text(container, SWT.BORDER | SWT.SINGLE);
		osgiPort.setText(info.getOsgiPort());
		osgiPort.setLayoutData(new GridData(100, textHeight));
	}

	private void addProfile() {
		Label profileLabel = new Label(container, SWT.NONE);
		profileLabel.setText("Profile");

		profile = new Combo(container, SWT.BORDER | SWT.SINGLE);
		final List<String> profiles = getProfiles(); 
		for(String name : profiles) {
			profile.add(name);
		}
		profile.add("other");
		index = getSelectedProfile(profiles);
		if(index != -1) {
			profile.select(index);	
		}

		GridData profileData = new GridData(135, textHeight);
		profileData.horizontalSpan = 1;
		profile.setLayoutData(profileData);
		profile.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				index=profile.getSelectionIndex();
				if(profile.getItem(index).equalsIgnoreCase("other")){
					info.setexternalProfile(true);
					externalProfileLoc.setEnabled(true);
				}
				else{
					externalProfileLoc.setEnabled(false);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});	
		addexternalProfileBox();
		addRedeployBox();
		addBackupEarBox();
		
	}

	private void addexternalProfileBox() {

		Label externalProfileLocLabel = new Label(container, SWT.NONE);
		externalProfileLocLabel.setText("External Profile Location");
		externalProfileLoc = new Text(container, SWT.BORDER | SWT.SINGLE);
		externalProfileLoc.setText(info.getexternalProfileLoc());
		GridData externalProfileLocData = new GridData(300, textHeight);
		externalProfileLoc.setLayoutData(externalProfileLocData);
		externalProfileLoc.setEnabled(false);

	}

	private void addRedeployBox() {
		
		Label domainLabel = new Label(container, SWT.NONE);
		domainLabel.setText("Redeploy the Application if exists");
		domainLabel.setToolTipText("Redeploy the Application if exists.");

		GridData deployData = new GridData(250, 25);
		deployData.horizontalSpan = 1;
		domainLabel.setLayoutData(deployData);
		
		redeploy = new Button(container, SWT.CHECK);
		redeploy.setSelection(info.isRedeploy());
		redeploy.setToolTipText("If this is checked, then the Application will be redeployed if exists.");
		GridData redeployData = new GridData();
		redeployData.horizontalSpan = 3;
		redeploy.setLayoutData(redeployData);
		
		
	}

	private void addBackupEarBox() {
		Label backupLabel = new Label(container, SWT.NONE);
		backupLabel.setText("Backup Application EAR if exists");
		backupLabel.setToolTipText("Backup Application EAR if exists.");
		
		backup = new Button(container, SWT.CHECK);
		backup.setSelection(info.isBackup());
		backup.setToolTipText("If this is checked, then the Application EAR will be backed up if exists.");
		
		GridData backupData = new GridData(250, 25);
		backupData.horizontalAlignment = GridData.BEGINNING;
		backupData.horizontalSpan = 0;
		backupLabel.setLayoutData(backupData);

		Label backupLocLabel = new Label(container, SWT.NONE);
		backupLocLabel.setText("Backup Location");
		backupLocation = new Text(container, SWT.BORDER | SWT.SINGLE);
		backupLocation.setText(info.getBackupLocation());
		GridData backupLocationData = new GridData(300, textHeight);
		//backupLocationData.horizontalAlignment = GridData.FILL;

		backupLocation.setLayoutData(backupLocationData);
		backupLocation.setEnabled(false);
		backup.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(info.isBackup()==false){
					info.setBackup(true);
				}
				else{
					info.setBackup(false);
				}
				backup.setSelection(info.isBackup());
				if(backup.getSelection()) {
					backupLocation.setEnabled(true);
				} else {
					backupLocation.setText("");
					backupLocation.setEnabled(false);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}

	private int getSelectedProfile(List<String> profiles) {
		if(info.getProfile() != null && !info.getProfile().isEmpty()) {
			if(profiles.contains(info.getProfile())) {
				return profiles.indexOf(info.getProfile());	
			}
		}
		String os = System.getProperty("os.name");
		boolean isWindows = false;
		if (os.indexOf("Windows") != -1) {
			isWindows = true;
		}

		if(isWindows && profiles.contains("WindowsProfile.substvar")) {
			return profiles.indexOf("WindowsProfile.substvar");
		} else if(!isWindows && profiles.contains("UnixProfile.substvar")) {
			return profiles.indexOf("UnixProfile.substvar");
		} else if(profiles.size() == 1) {
			return 0;
		} else {
			if(profiles.contains("default.substvar")) {
				return profiles.indexOf("default.substvar");	
			}
		}
		return -1;
	}

	private List<String> getProfiles() {
		File appProject = new File(ModuleHelper.getApplication(project.getModules()).getProject().getLocationURI());
		File metainf = new File (appProject, "META-INF");
		File[] files = metainf.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.getName().indexOf(".substvar") != -1) {
        			return true;
				}
				return false;
			}
		});
		List<String> list = new ArrayList<String>();
		for(File file : files) {
			list.add(file.getName());
		}
		return list;
	}
	@Override
	public boolean canFlipToNextPage() 
	{
		return false;
	}
	
	@Override
	public void performHelp() {
		// TODO Auto-generated method stub
		super.performHelp();
		
		try {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL("https://github.com/TIBCOSoftware/bw6-plugin-maven/wiki"));
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
