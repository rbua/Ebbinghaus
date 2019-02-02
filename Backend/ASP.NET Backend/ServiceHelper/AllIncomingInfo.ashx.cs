using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web;
using Newtonsoft.Json;

namespace TEST_magiword_dev_preview.ServiceHelper
{
    public class AllIncomingInfo : IHttpHandler
    {
        private readonly bool logQueries;
        public AllIncomingInfo(bool logqueries)
        {
            logQueries = logqueries;
        }

        public void ProcessRequest(HttpContext context)
        {
            throw new NotImplementedException();
            //var outer = new { lst = new List<int>() };
            //JsonConvert.SerializeObject(outer);
            //var getjson = new JSONhelper();
            //JSONhelper postjson = new JSONhelper();

            //foreach (var item in context.Request.QueryString.AllKeys)
            //{
            //    getjson.AddPair(item, context.Request.QueryString[item]);
            //}
            //getjson.Finish();

            //foreach (var item in context.Request.Form.AllKeys)
            //{
            //    postjson.AddPair(item, context.Request.Form[item]);
            //}
            //postjson.Finish();

            //outer.AddPair("get arguments", getjson);
            //outer.AddPair("post arguments", postjson);
            //outer.Finish();

            //context.Response.Write(outer.OutputJSON.ToString());

            //if (logQueries)
            //{
            //    string createText = outer.OutputJSON.ToString() + Environment.NewLine;
            //    string fullPath = System.Web.HttpContext.Current.Server.MapPath("~") + @"App_Data\Queries.txt";
            //    using (StreamWriter sw = new StreamWriter(fullPath, true, System.Text.Encoding.Default))
            //    {
            //        sw.WriteLine(createText + "   ip = " +
            //        context.Request.UserHostAddress + "     useragent: " + context.Request.UserAgent + new string('\n', 3));
            //    }
            //}
        }

        public bool IsReusable
        { get { return false; } }
    }
}