import { getLanguageSpecific } from './utils';

export class Product {
  constructor(name) {
    cy.log(`Product - set name = ${name}`);
    this.name = name;
    this.attributeValues = [];
  }

  setName(name) {
    cy.log(`Product - set name = ${name}`);
    this.name = name;
    return this;
  }

  setValue(value) {
    cy.log(`Product - set value = ${value}`);
    this.value = value;
    return this;
  }

  setDescription(description) {
    cy.log(`Product - set description = ${description}`);
    this.description = description;
    return this;
  }

  setStocked(isStocked) {
    cy.log(`Product - set isStocked = ${isStocked}`);
    this.isStocked = isStocked;
    return this;
  }

  setPurchased(isPurchased) {
    cy.log(`Product - set isPurchased = ${isPurchased}`);
    this.isPurchased = isPurchased;
    return this;
  }

  setSold(isSold) {
    cy.log(`Product - set isSold = ${isSold}`);
    this.isSold = isSold;
    return this;
  }

  setDiverse(isDiverse) {
    cy.log(`Product - set isDiverse = ${isDiverse}`);
    this.isDiverse = isDiverse;
    return this;
  }

  setProductCategory(m_product_category) {
    cy.log(`Product - set productCategory = ${m_product_category}`);
    this.m_product_category = m_product_category;
    return this;
  }

  setProductType(productType) {
    cy.log(`Product - set productType = ${productType}`);
    this.productType = productType;
    return this;
  }

  setUOM(c_uom) {
    cy.log(`Product - set c_uom = ${c_uom}`);
    this.c_uom = c_uom;
    return this;
  }

  apply() {
    cy.log(`Product - apply - START (name=${this.name})`);
    applyProduct(this);
    cy.log(`Product - apply - END (name=${this.name})`);
    return this;
  }
}

function applyProduct(product) {
  describe(`Create new Product ${product.name}`, function() {
    cy.visitWindow('140', 'NEW');
    cy.writeIntoStringField('Name', product.name);

    // Value is readonly
    //cy.clearField('Value');
    //cy.writeIntoStringField('Value', product.value);

    cy.selectInListField('M_Product_Category_ID', product.m_product_category);

    cy.writeIntoStringField('Description', product.description);

    cy.getStringFieldValue('IsStocked').then(isIsStockedValue => {
      if (product.isStocked && !isIsStockedValue) {
        cy.clickOnCheckBox('IsStocked');
      }
    });
    cy.getStringFieldValue('IsPurchased').then(isPurchasedValue => {
      if (product.isPurchased && !isPurchasedValue) {
        cy.clickOnCheckBox('IsPurchased');
      }
    });
    cy.getStringFieldValue('IsSold').then(isSoldValue => {
      if (product.isSold && !isSoldValue) {
        cy.clickOnCheckBox('IsSold');
      }
    });
    cy.getStringFieldValue('IsDiverse').then(isDiverseValue => {
      if (product.isDiverse && !isDiverseValue) {
        cy.clickOnCheckBox('IsDiverse');
      }
    });

    cy.getStringFieldValue('ProductType').then(productTypeValue => {
      const productType = getLanguageSpecific(product, 'productType');

      if (productType != productTypeValue) {
        cy.selectInListField('ProductType', productType);
      }
    });

    cy.getStringFieldValue('C_UOM_ID').then(uomValue => {
      const c_uom = getLanguageSpecific(product, 'c_uom');

      if (c_uom && c_uom != uomValue) {
        cy.selectInListField('C_UOM_ID', c_uom);
      }
    });

    if (product.prices.length > 0) {
      product.prices.forEach(function(price) {
        applyProductPrice(price);
      });
      cy.get('table tbody tr').should('have.length', product.prices.length);
    }
  });
}

export class ProductCategory {
  constructor(name) {
    cy.log(`Product Category - set name = ${name}`);
    this.name = name;
  }

  setName(name) {
    cy.log(`Product Category - set name = ${name}`);
    this.name = name;
    return this;
  }

  setValue(value) {
    cy.log(`Product Category - set value = ${value}`);
    this.value = value;
    return this;
  }

  apply() {
    cy.log(`Product Category - apply - START (name=${this.name})`);
    applyProductCategory(this);
    cy.log(`Product Category - apply - END (name=${this.name})`);
    return this;
  }
}

function applyProductCategory(productCategory) {
  describe(`Create new Product ${productCategory.name}`, function() {
    cy.visitWindow('144', 'NEW');
    cy.writeIntoStringField('Name', productCategory.name);

    // Value is updateable
    cy.writeIntoStringField('Value', productCategory.value);
  });
}

function applyProductPrice(price) {
  describe(`Create new Product Price ${price.m_pricelist_version}`, function() {
    const m_pricelist_version = getLanguageSpecific(price, 'm_pricelist_version');

    cy.get('#tab_M_ProductPrice').click();
    cy.pressAddNewButton();
    cy.writeIntoLookupListField(
      'M_PriceList_Version_ID',
      m_pricelist_version,
      m_pricelist_version,
      false /*typeList*/,
      true /*modal*/
    );

    cy.writeIntoStringField('PriceStd', price.priceStd, true /*modal*/, null /*rewriteUrl*/, true /*noRequest*/);

    cy.selectInListField('C_TaxCategory_ID', getLanguageSpecific(price, 'c_taxcategory'), true);
    cy.pressDoneButton();
  });
}
