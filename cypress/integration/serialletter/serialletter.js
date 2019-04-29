it('Serial letter', function() {
  const timestamp = new Date().getTime(); // used in the document names, for ordering
  const printFormatName = `${timestamp} Serial letter printformat`;
  const marketingPlatformName = `${timestamp} Serial letter marketing platform`;
  const boilerPlateName = `${timestamp}_Serial_letter_text_snippet`;

  const marketingCampaignName = `${timestamp} Serial letter marketing campaign`;
  const marketingContactName = `${timestamp} Serial letter marketing contact`;

  //it('Create print format and doc outbound log', function() {
  cy.visit('/window/240/NEW');
  cy.writeIntoStringField('Name', printFormatName);
  cy.writeIntoLookupListField('AD_Table_ID', 'Letter', 'Letter');
  cy.writeIntoLookupListField('JasperProcess_ID', 'Serial Letter', 'Serial Letter');

  cy.visit('/window/540173/NEW');
  cy.writeIntoLookupListField('AD_Table_ID', 'Letter', 'Letter');
  cy.writeIntoLookupListField('AD_PrintFormat_ID', printFormatName, printFormatName);
  cy.clickOnCheckBox('IsCreatePrintJob');
  //cy.wait(500);
  //});

  //it('creates Marketing platform', function() {
  cy.visit('/window/540437/NEW');
  cy.writeIntoStringField('Name', marketingPlatformName);
  cy.clickOnCheckBox('IsRequiredLocation');
  //cy.wait(500);
  //});

  //it('Creates boiler plate (text-snippet)', function() {
  cy.visit('/window/504410/NEW');
  cy.writeIntoStringField('Name', boilerPlateName);
  cy.writeIntoStringField('Subject', 'Subject');
  cy.writeIntoTextField('TextSnippet', 'TextSnippet');
  cy.selectTab('AD_BoilerPlate_Ref'); // need to click somewhere outside of the text field
  //});

  //it('Creates contact person', function() {
  cy.visit('/window/540435/NEW');
  cy.writeIntoStringField('Name', marketingContactName);
  cy.writeIntoLookupListField('MKTG_Platform_ID', marketingPlatformName, marketingPlatformName);
  cy.editAddress('C_Location_ID', function(url) {
    cy.writeIntoStringField('City', 'Cologne', null, url);
    cy.writeIntoLookupListField('C_Country_ID', 'Deu', 'Deutschland', false /*typeList */, false /*modal */, url);
  });
  //});

  //it('Create marketing campaign', function() {
  cy.visit('/window/540434/NEW');
  cy.writeIntoStringField('Name', marketingCampaignName);
  cy.writeIntoLookupListField('MKTG_Platform_ID', marketingPlatformName, marketingPlatformName);
  cy.writeIntoLookupListField('AD_BoilerPlate_ID', boilerPlateName, boilerPlateName);
  //});

  //it('add contact person to campaign', function() {
  cy.selectTab('MKTG_Campaign_ContactPerson');
  cy.pressAddNewButton();
  cy.writeIntoLookupListField('MKTG_ContactPerson_ID', marketingContactName, marketingContactName);
  cy.pressDoneButton();

  // needs https://github.com/metasfresh/metasfresh-webui-frontend/issues/1978#issuecomment-445274540
  cy.executeHeaderAction('C_Letter_CreateFrom_MKTG_ContactPerson');
  //});
});
