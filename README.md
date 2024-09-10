<h1>For devs:</h1>

<h3>Saving</h3>
<p>Saving is handeled in org.astro.core.saving, you must call register({the object that implements "Savable" also from saving.Savable interface}) on the static saving.SaveManager.<br>
The saving.Savable interface has 2 functions load and save, in the load function a argument is the object loaded from the save file in the save you must return the object you wish to save. <b>THIS OBJECT MUST BE SERIALIZABLE!</b></p>

<h3>Items</h3>
<p>Adding items requires adding code to the org.astro.core.Items.java class.
<ul>
  <li>add a inventory item object to the inventoryItemsP map in Items.java wich will represent the item in inventories</li>
  <li>add a physical item object to the physicalItemsP map in Items.java wich will represent the item when it is "on the floor"</li>
  <li>add a description of the item to the itemDescriptions map in Items.java</li>
</ul>
</p>
