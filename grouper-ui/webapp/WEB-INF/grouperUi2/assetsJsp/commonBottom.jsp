
<script src="../../grouperExternal/public/assets/dojo/dojo/dojo.js"></script>

<script type="text/javascript" >
    dojo.require("dojo/ready");
    dojo.require("dojo/parser");
    dojo.require("dijit/form/FilteringSelect");
    dojo.require("dojox/data/QueryReadStore");
    dojo.require("dojo/dom-attr");
    dojo.require("dijit.Tree");
    dojo.require("dojo.data.ItemFileReadStore");
</script>

<script src="../../grouperExternal/public/assets/js/jquery.js"></script>
<script src="../../grouperExternal/public/assets/js/bootstrap.js"></script>
<script src="../../grouperExternal/public/assets/js/tree.jquery.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.dataTables.min.js"></script>
<script src="../../grouperExternal/public/assets/js/footable-0.1.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.cookie.js"></script>
<script src="../../grouperExternal/public/assets/js/grouper.js"></script>
<script src="../../grouperExternal/public/assets/js/grouperUi.js"></script>
<script src="../../grouperExternal/public/assets/js/wz_tooltip.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.blockUI.js"></script>
<script src="../../grouperExternal/public/assets/js/jquery.simplemodal.js"></script>
<script src="../../grouperExternal/public/assets/nifty/niftycube.js"></script>

    <script type="text/javascript">
        $().ajaxStop($.unblockUI); 
        $.blockUI.defaults.message = "<img src='../../grouperExternal/public/assets/images/busy.gif' alt='busy'/>";
        $.blockUI.defaults.css.border = 'none';
        $.blockUI.defaults.css.backgroundColor = 'transparent';
        $.blockUI.defaults.overlayCSS.opacity = '0.02';
        $.blockUI.defaults.fadeIn = '200';
        $.blockUI.defaults.fadeOut = '400';
        $.blockUI.defaults.timeout = '180000';
    </script>

