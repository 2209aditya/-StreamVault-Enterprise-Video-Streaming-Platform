resource "azurerm_postgresql_flexible_server" "db" {
  name                   = var.name
  resource_group_name    = var.rg_name
  location               = var.location
  administrator_login    = "adminuser"
  administrator_password = "Password1234!"
  sku_name               = "Standard_B2s"
  version                = "15"
}