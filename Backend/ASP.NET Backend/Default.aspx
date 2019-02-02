<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="Default.aspx.cs" Inherits="TEST_magiword_dev_preview.Default" %>

<!DOCTYPE html>

<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title></title>
</head>
<body>
    <form id="form1" runat="server">
        <div>
            <a href="TranslationHelper/GetTranslate.ashx">HEY BRO, LINK IS HERE</a>
        </div>
        <asp:TextBox ID="TextBox1" runat="server"></asp:TextBox>
        <asp:Button runat="server" ID="GetJson" Text="GetJson" OnClick="GetJson_Click" />
        <hr />
        <asp:Label runat="server" ID="Lab" Text="   "></asp:Label>
        <hr />
        <asp:Label runat="server" ID="Meta" Text="   "></asp:Label>


        <asp:Button ID="Button1" runat="server" Text="check connection" OnClick="Button1_Click" />
    </form>
</body>
</html>
