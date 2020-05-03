package com.company.bbkb.entity;

public class TtlProductInfoPo {

    private Long id;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Long branchId;
    private String branchName;
    private Long shopId;
    private String shopName;
    private Double price;
    private Integer stock;
    private Integer salesNum;
    private String createTime;
    private String updateTime;
    private Byte isDel;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public Long getBranchId() {
    return branchId;
  }

  public void setBranchId(Long branchId) {
    this.branchId = branchId;
  }

  public String getBranchName() {
    return branchName;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public Long getShopId() {
    return shopId;
  }

  public void setShopId(Long shopId) {
    this.shopId = shopId;
  }

  public String getShopName() {
    return shopName;
  }

  public void setShopName(String shopName) {
    this.shopName = shopName;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer stock) {
    this.stock = stock;
  }

  public Integer getSalesNum() {
    return salesNum;
  }

  public void setSalesNum(Integer salesNum) {
    this.salesNum = salesNum;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

  public Byte getIsDel() {
    return isDel;
  }

  public void setIsDel(Byte isDel) {
    this.isDel = isDel;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TtlProductInfoPo{");
    sb.append("id=").append(id);
    sb.append(", productName='").append(productName).append('\'');
    sb.append(", categoryId=").append(categoryId);
    sb.append(", categoryName='").append(categoryName).append('\'');
    sb.append(", branchId=").append(branchId);
    sb.append(", branchName='").append(branchName).append('\'');
    sb.append(", shopId=").append(shopId);
    sb.append(", shopName='").append(shopName).append('\'');
    sb.append(", price=").append(price);
    sb.append(", stock=").append(stock);
    sb.append(", salesNum=").append(salesNum);
    sb.append(", createTime='").append(createTime).append('\'');
    sb.append(", updateTime='").append(updateTime).append('\'');
    sb.append(", isDel=").append(isDel);
    sb.append('}');
    return sb.toString();
  }
}
